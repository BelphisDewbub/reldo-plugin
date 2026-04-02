package com.reldoapp;

import com.google.inject.Provides;
import com.reldoapp.data.CombatAchievementMapper;
import com.reldoapp.data.DiaryMapper;
import com.reldoapp.data.LeagueTaskMapper;
import com.reldoapp.data.QuestMapper;
import com.reldoapp.data.SkillMapper;
import com.reldoapp.sync.SyncResult;
import com.reldoapp.sync.SyncService;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Reldo",
	description = "Sync your OSRS character data to Reldo",
	tags = {"tracker", "quests", "leagues", "diary", "reldo"}
)
public class ReldoPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ReldoConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private SyncService syncService;

	private ReldoPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp()
	{
		panel = injector.getInstance(ReldoPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "reldo_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Reldo")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		log.debug("Reldo plugin started");
	}

	@Override
	protected void shutDown()
	{
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}
		panel = null;
		navButton = null;
		log.debug("Reldo plugin stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && config.syncOnLogin())
		{
			// Delay 3s — varbits are not fully populated immediately on login
			executor.schedule(() -> clientThread.invoke(this::doSync), 3, TimeUnit.SECONDS);
		}
	}

	/**
	 * Trigger a sync from outside the client thread (e.g., panel button click).
	 */
	void triggerSync()
	{
		clientThread.invoke(this::doSync);
	}

	/**
	 * Collect game state on the client thread, then fire the HTTP call on the executor.
	 * Must be called from the client thread.
	 */
	private void doSync()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			SwingUtilities.invokeLater(() -> panel.setStatus("Not logged in."));
			return;
		}

		String playerName = client.getLocalPlayer().getName();
		if (playerName == null || playerName.isEmpty())
		{
			SwingUtilities.invokeLater(() -> panel.setStatus("Could not read player name."));
			return;
		}

		SwingUtilities.invokeLater(() -> panel.setStatus("Syncing…"));

		// Collect all game data while on the client thread
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("character_name", playerName);
		payload.put("skills", SkillMapper.collectSkills(client));
		payload.put("quests", QuestMapper.collectQuests(client));
		payload.put("achievement_diaries", DiaryMapper.collectDiaries(client));
		payload.put("combat_achievements", CombatAchievementMapper.collectCombatAchievements(client));

		League league = config.activeLeague();
		if (league != League.NONE)
		{
			String leagueId = league.getLeagueId();
			Map<String, Object> taskMap = LeagueTaskMapper.collectTasks(client, leagueId);
			if (taskMap != null)
			{
				Map<String, Object> leaguePayload = new LinkedHashMap<>();
				leaguePayload.put("tasks", taskMap);
				leaguePayload.put("skills", SkillMapper.collectSkills(client));
				payload.put("leagues", Map.of(leagueId, leaguePayload));
			}
		}

		// Hand off to background thread for the HTTP call
		executor.submit(() ->
		{
			SyncResult result = syncService.send(payload);
			SwingUtilities.invokeLater(() -> panel.showResult(result));
		});
	}

	@Provides
	ReldoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ReldoConfig.class);
	}
}
