package com.kaze943.oxycarbide.market;

import com.google.gson.JsonElement;

/**
 * An item that has to be handed back to its owner the next time they are online
 * (market withdrawal by an operator, expiry, ...).
 */
public final class PendingReturn {
	public String owner = "";
	public String itemName = "";
	public JsonElement item;
}
