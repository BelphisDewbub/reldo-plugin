package com.reldoapp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("reldo")
public interface ReldoConfig extends Config
{
	@ConfigSection(
		name = "Connection",
		description = "Settings for connecting to your Reldo instance",
		position = 0
	)
	String connectionSection = "connection";

	@ConfigItem(
		keyName = "apiToken",
		name = "API Token",
		description = "Generate this in Reldo → Settings → Plugin API Token",
		secret = true,
		section = connectionSection,
		position = 1
	)
	default String apiToken()
	{
		return "";
	}

	@ConfigItem(
		keyName = "baseUrl",
		name = "Reldo URL",
		description = "Base URL of your Reldo instance (no trailing slash)",
		section = connectionSection,
		position = 2
	)
	default String baseUrl()
	{
		return "https://reldo.app";
	}

	@ConfigSection(
		name = "Sync",
		description = "What to sync and when",
		position = 10
	)
	String syncSection = "sync";

	@ConfigItem(
		keyName = "syncOnLogin",
		name = "Sync on login",
		description = "Automatically sync when you log in",
		section = syncSection,
		position = 11
	)
	default boolean syncOnLogin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "activeLeague",
		name = "Active League",
		description = "League to sync tasks for. Select None to skip league sync.",
		section = syncSection,
		position = 12
	)
	default League activeLeague()
	{
		return League.NONE;
	}
}
