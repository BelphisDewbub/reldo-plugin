package com.reldoapp;

public enum League
{
	NONE("", "None (skip league sync)"),
	RAGING_ECHOES("raging_echoes", "Raging Echoes"),
	DEMONIC_PACTS("demonic_pacts", "Demonic Pacts");

	private final String leagueId;
	private final String displayName;

	League(String leagueId, String displayName)
	{
		this.leagueId = leagueId;
		this.displayName = displayName;
	}

	public String getLeagueId()
	{
		return leagueId;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
