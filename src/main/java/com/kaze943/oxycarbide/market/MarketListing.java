package com.kaze943.oxycarbide.market;

import com.google.gson.JsonElement;

/**
 * One player listing on the global market.
 *
 * <p>The sold item is stored as a serialized {@code ItemStack} JSON so enchantments and
 * other components survive a restart.</p>
 */
public final class MarketListing {
	/** Ten digit numeric id. */
	public String id = "";
	public String seller = "";
	public String sellerName = "";
	public String itemName = "";
	public JsonElement item;
	public String currency = "normal";
	public long price;
	public long createdAt;
	public long expiresAt;
}
