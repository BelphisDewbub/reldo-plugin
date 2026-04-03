package com.reldoapp.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class CollectionLogTracker
{
	/**
	 * Fires once per collection log item as the UI renders it.
	 * args[1] = itemId (int), args[2] = quantity (int).
	 * Only fires for items the player has actually obtained.
	 */
	private static final int COLLECTION_LOG_ITEM_SCRIPT = 4100;

	private static final Pattern NEW_ITEM_PATTERN =
		Pattern.compile("New item added to your collection log: (.+)");
	private static final String CONFIG_GROUP = "reldo";
	private static final String CONFIG_KEY   = "collectionLog";

	@Inject private Client client;
	@Inject private ConfigManager configManager;
	@Inject private Gson gson;

	/** Keyed by item ID. */
	private final Map<Integer, CollectionLogEntry> items = new HashMap<>();

	/** Tick on which script 4100 last fired; -1 = not pending. */
	private int lastScriptTick = -1;
	/** Whether any item changed during the current script batch. */
	private boolean batchChanged = false;

	/** Called after any update so the plugin can fire an immediate sync. */
	private Runnable syncCallback;

	public void setSyncCallback(Runnable cb)
	{
		this.syncCallback = cb;
	}

	// ── Event listeners ───────────────────────────────────────────────────────

	/**
	 * Chat message fires in real time when a new item is obtained.
	 * Only the name is available here — script 4100 will supply the ID + quantity
	 * when the player next opens the collection log.
	 */
	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		Matcher m = NEW_ITEM_PATTERN.matcher(Text.removeTags(event.getMessage()));
		if (!m.matches())
		{
			return;
		}
		String name = m.group(1).trim();

		// Store name-only entry (id=-1) if not already known by ID
		boolean alreadyKnown = items.values().stream()
			.anyMatch(e -> e.name.equalsIgnoreCase(name));
		if (!alreadyKnown)
		{
			// Use a temporary negative key so it doesn't collide with real IDs
			int tempKey = -(name.hashCode());
			items.put(tempKey, new CollectionLogEntry(-1, name, 1));
			persist();
			fireCallback();
		}
	}

	/**
	 * Script 4100 fires once per obtained item when the collection log UI renders.
	 * args[1] = itemId, args[2] = quantity.
	 * Guard against the POH adventure log (viewing another player's log).
	 * Does NOT sync immediately — batches until onGameTick sees a 2-tick gap.
	 */
	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (event.getScriptId() != COLLECTION_LOG_ITEM_SCRIPT)
		{
			return;
		}

		// Don't process when viewing another player's log via POH adventure log
		if (client.getVarbitValue(VarbitID.COLLECTION_POH_HOST_BOOK_OPEN) == 1)
		{
			return;
		}

		Object[] args = event.getScriptEvent().getArguments();
		if (args == null || args.length < 3)
		{
			return;
		}

		int id  = (int) args[1];
		int qty = (int) args[2];
		if (id <= 0 || qty <= 0)
		{
			return;
		}

		String name = client.getItemDefinition(id).getName();

		CollectionLogEntry existing = items.get(id);
		if (existing == null || qty > existing.quantity)
		{
			items.put(id, new CollectionLogEntry(id, name, qty));
			// Remove any name-only temp entry for this item
			items.entrySet().removeIf(e -> e.getKey() < 0 && e.getValue().name.equalsIgnoreCase(name));
			batchChanged = true;
		}

		// Record the tick so onGameTick knows script is still firing
		lastScriptTick = client.getTickCount();
	}

	/**
	 * After script 4100 stops firing for 2 ticks, persist and sync once.
	 */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (lastScriptTick == -1 || !batchChanged)
		{
			return;
		}
		if (client.getTickCount() >= lastScriptTick + 2)
		{
			lastScriptTick = -1;
			batchChanged = false;
			persist();
			fireCallback();
		}
	}

	// ── Public API ────────────────────────────────────────────────────────────

	public List<CollectionLogEntry> getItems()
	{
		return new ArrayList<>(items.values());
	}

	// ── Persistence ───────────────────────────────────────────────────────────

	private void persist()
	{
		try
		{
			String json = gson.toJson(items);
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
		}
		catch (Exception e)
		{
			log.warn("Failed to persist collection log data", e);
		}
	}

	public void load()
	{
		try
		{
			String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
			if (json == null || json.isEmpty())
			{
				return;
			}
			Type type = new TypeToken<Map<Integer, CollectionLogEntry>>(){}.getType();
			Map<Integer, CollectionLogEntry> loaded = gson.fromJson(json, type);
			if (loaded != null)
			{
				items.putAll(loaded);
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to load collection log data", e);
		}
	}

	// ── Helpers ───────────────────────────────────────────────────────────────

	private void fireCallback()
	{
		if (syncCallback != null)
		{
			syncCallback.run();
		}
	}
}
