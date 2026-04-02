package com.reldoapp;

import com.reldoapp.sync.SyncResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class ReldoPanel extends PluginPanel
{
	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final JLabel statusLabel;
	private final JButton syncButton;
	private final ReldoPlugin plugin;

	@Inject
	public ReldoPanel(ReldoPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		JLabel title = new JLabel("Reldo Sync");
		title.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 16f));
		title.setForeground(new Color(255, 200, 0));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		add(title, BorderLayout.NORTH);

		// Centre panel: status + button
		JPanel centre = new JPanel(new BorderLayout(0, 6));
		centre.setBackground(ColorScheme.DARK_GRAY_COLOR);

		statusLabel = new JLabel("Ready.");
		statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		statusLabel.setFont(FontManager.getRunescapeSmallFont());
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		centre.add(statusLabel, BorderLayout.NORTH);

		syncButton = new JButton("Sync Now");
		syncButton.setFocusPainted(false);
		syncButton.setBackground(new Color(50, 50, 50));
		syncButton.setForeground(Color.WHITE);
		syncButton.addActionListener(e ->
		{
			syncButton.setEnabled(false);
			statusLabel.setText("Syncing…");
			plugin.triggerSync();
		});
		centre.add(syncButton, BorderLayout.SOUTH);

		add(centre, BorderLayout.CENTER);

		// Footer note
		JLabel note = new JLabel("<html><center>Configure your API token<br>in the plugin settings.</center></html>");
		note.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		note.setFont(FontManager.getRunescapeSmallFont());
		note.setHorizontalAlignment(SwingConstants.CENTER);
		add(note, BorderLayout.SOUTH);
	}

	public void setStatus(String message)
	{
		statusLabel.setText(message);
		syncButton.setEnabled(true);
	}

	public void showResult(SyncResult result)
	{
		String time = LocalTime.now().format(TIME_FMT);
		if (result.isSuccess())
		{
			statusLabel.setForeground(new Color(0, 200, 83));
			statusLabel.setText("Synced at " + time);
		}
		else
		{
			statusLabel.setForeground(new Color(220, 50, 50));
			statusLabel.setText("<html><center>" + result.getMessage() + "</center></html>");
		}
		syncButton.setEnabled(true);
	}
}
