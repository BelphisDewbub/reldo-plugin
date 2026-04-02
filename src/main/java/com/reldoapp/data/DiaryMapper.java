package com.reldoapp.data;

import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Varbits;

public class DiaryMapper
{
	/**
	 * Each row: { easy varbit, medium varbit, hard varbit, elite varbit }
	 * Varbit returns 1 when all tasks in that tier are completed.
	 * Order matches REGION_NAMES below.
	 *
	 * Varbit IDs sourced from net.runelite.api.Varbits and the OSRS wiki.
	 */
	private static final int[][] TIER_VARBITS = {
		// Ardougne
		{Varbits.DIARY_ARDOUGNE_EASY, Varbits.DIARY_ARDOUGNE_MEDIUM, Varbits.DIARY_ARDOUGNE_HARD, Varbits.DIARY_ARDOUGNE_ELITE},
		// Desert
		{Varbits.DIARY_DESERT_EASY, Varbits.DIARY_DESERT_MEDIUM, Varbits.DIARY_DESERT_HARD, Varbits.DIARY_DESERT_ELITE},
		// Falador
		{Varbits.DIARY_FALADOR_EASY, Varbits.DIARY_FALADOR_MEDIUM, Varbits.DIARY_FALADOR_HARD, Varbits.DIARY_FALADOR_ELITE},
		// Fremennik
		{Varbits.DIARY_FREMENNIK_EASY, Varbits.DIARY_FREMENNIK_MEDIUM, Varbits.DIARY_FREMENNIK_HARD, Varbits.DIARY_FREMENNIK_ELITE},
		// Kandarin
		{Varbits.DIARY_KANDARIN_EASY, Varbits.DIARY_KANDARIN_MEDIUM, Varbits.DIARY_KANDARIN_HARD, Varbits.DIARY_KANDARIN_ELITE},
		// Karamja
		{Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM, Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE},
		// Kourend & Kebos
		{Varbits.DIARY_KOUREND_EASY, Varbits.DIARY_KOUREND_MEDIUM, Varbits.DIARY_KOUREND_HARD, Varbits.DIARY_KOUREND_ELITE},
		// Lumbridge & Draynor
		{Varbits.DIARY_LUMBRIDGE_EASY, Varbits.DIARY_LUMBRIDGE_MEDIUM, Varbits.DIARY_LUMBRIDGE_HARD, Varbits.DIARY_LUMBRIDGE_ELITE},
		// Morytania
		{Varbits.DIARY_MORYTANIA_EASY, Varbits.DIARY_MORYTANIA_MEDIUM, Varbits.DIARY_MORYTANIA_HARD, Varbits.DIARY_MORYTANIA_ELITE},
		// Tirannwn
		{Varbits.DIARY_TIRANNWN_EASY, Varbits.DIARY_TIRANNWN_MEDIUM, Varbits.DIARY_TIRANNWN_HARD, Varbits.DIARY_TIRANNWN_ELITE},
		// Varrock
		{Varbits.DIARY_VARROCK_EASY, Varbits.DIARY_VARROCK_MEDIUM, Varbits.DIARY_VARROCK_HARD, Varbits.DIARY_VARROCK_ELITE},
		// Western Provinces
		{Varbits.DIARY_WESTERN_EASY, Varbits.DIARY_WESTERN_MEDIUM, Varbits.DIARY_WESTERN_HARD, Varbits.DIARY_WESTERN_ELITE},
		// Wilderness
		{Varbits.DIARY_WILDERNESS_EASY, Varbits.DIARY_WILDERNESS_MEDIUM, Varbits.DIARY_WILDERNESS_HARD, Varbits.DIARY_WILDERNESS_ELITE},
	};

	/** Region names matching keys in Reldo's achievement_diaries field. */
	private static final String[] REGION_NAMES = {
		"Ardougne",
		"Desert",
		"Falador",
		"Fremennik",
		"Kandarin",
		"Karamja",
		"Kourend & Kebos",
		"Lumbridge & Draynor",
		"Morytania",
		"Tirannwn",
		"Varrock",
		"Western Provinces",
		"Wilderness",
	};

	private static final String[] TIER_KEYS = {"easy", "medium", "hard", "elite"};

	/**
	 * Collect achievement diary tier completions for all 13 regions.
	 * Only includes tiers that are completed (true). Incomplete tiers are omitted.
	 *
	 * Must be called from the client thread.
	 */
	public static Map<String, Map<String, Boolean>> collectDiaries(Client client)
	{
		Map<String, Map<String, Boolean>> result = new LinkedHashMap<>();

		for (int r = 0; r < REGION_NAMES.length; r++)
		{
			Map<String, Boolean> tiers = new LinkedHashMap<>();
			for (int t = 0; t < TIER_KEYS.length; t++)
			{
				if (client.getVarbitValue(TIER_VARBITS[r][t]) == 1)
				{
					tiers.put(TIER_KEYS[t], true);
				}
			}
			if (!tiers.isEmpty())
			{
				result.put(REGION_NAMES[r], tiers);
			}
		}

		return result;
	}
}
