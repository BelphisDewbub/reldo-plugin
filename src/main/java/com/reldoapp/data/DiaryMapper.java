package com.reldoapp.data;

import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.Client;

public class DiaryMapper
{
	/**
	 * Each row: { easy varbit, medium varbit, hard varbit, elite varbit }
	 * Varbit returns 1 when all tasks in that tier are completed.
	 * Order matches REGION_NAMES below.
	 *
	 * Raw integer IDs sourced from the OSRS wiki Achievement Diary page.
	 * Using raw IDs rather than Varbits enum constants to avoid compile-time
	 * dependency on constants that may be missing or renamed across RuneLite versions.
	 */
	private static final int[][] TIER_VARBITS = {
		// Ardougne
		{4458, 4459, 4460, 4461},
		// Desert
		{4471, 4472, 4473, 4474},
		// Falador
		{4462, 4463, 4464, 4465},
		// Fremennik
		{4475, 4476, 4477, 4478},
		// Kandarin
		{4479, 4480, 4481, 4482},
		// Karamja
		{4483, 4484, 4485, 4486},
		// Kourend & Kebos
		{7925, 7926, 7927, 7928},
		// Lumbridge & Draynor
		{4487, 4488, 4489, 4490},
		// Morytania
		{4491, 4492, 4493, 4494},
		// Tirannwn
		{4495, 4496, 4497, 4498},
		// Varrock
		{4466, 4467, 4468, 4469},
		// Western Provinces
		{4499, 4500, 4501, 4502},
		// Wilderness
		{4503, 4504, 4505, 4506},
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
