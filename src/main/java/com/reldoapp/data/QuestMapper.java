package com.reldoapp.data;

import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

public class QuestMapper
{
	/**
	 * Collect completion state of every quest in the RuneLite Quest enum.
	 * Keys are display names matching Reldo's quests.json (e.g. "Dragon Slayer II").
	 * Values are Reldo status strings: "not_started", "in_progress", "completed".
	 *
	 * Must be called from the client thread.
	 */
	public static Map<String, String> collectQuests(Client client)
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (Quest quest : Quest.values())
		{
			QuestState state = quest.getState(client);
			result.put(quest.getName(), toReldoStatus(state));
		}
		return result;
	}

	private static String toReldoStatus(QuestState state)
	{
		switch (state)
		{
			case FINISHED:
				return "completed";
			case IN_PROGRESS:
				return "in_progress";
			default:
				return "not_started";
		}
	}
}
