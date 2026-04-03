package com.reldoapp.data;

/**
 * A single collection log item entry.
 * id is -1 if the item was detected only via chat message (no widget scan yet).
 */
public class CollectionLogEntry
{
	public final int id;
	public final String name;
	public final int quantity;

	public CollectionLogEntry(int id, String name, int quantity)
	{
		this.id = id;
		this.name = name;
		this.quantity = quantity;
	}
}
