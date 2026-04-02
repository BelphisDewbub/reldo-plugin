package com.reldoapp.data;

import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Skill;

public class SkillMapper
{
	/**
	 * Collect current skill levels and XP for all skills.
	 * Keys match Reldo's hiscores.skills keys (e.g. "Attack", "Runecraft").
	 * Overall is skipped — it is computed server-side from individual skills.
	 *
	 * When in a league world, getRealSkillLevel returns the league level,
	 * so this data is correct for both main game and seasonal play.
	 *
	 * Must be called from the client thread.
	 */
	public static Map<String, Map<String, Object>> collectSkills(Client client)
	{
		Map<String, Map<String, Object>> result = new LinkedHashMap<>();
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL)
			{
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("level", client.getRealSkillLevel(skill));
			entry.put("xp", client.getSkillExperience(skill));
			result.put(skill.getName(), entry);
		}
		return result;
	}
}
