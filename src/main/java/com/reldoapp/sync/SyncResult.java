package com.reldoapp.sync;

import lombok.Value;

@Value
public class SyncResult
{
	boolean success;
	String message;

	public static SyncResult ok()
	{
		return new SyncResult(true, "Synced successfully.");
	}

	public static SyncResult error(String message)
	{
		return new SyncResult(false, message);
	}
}
