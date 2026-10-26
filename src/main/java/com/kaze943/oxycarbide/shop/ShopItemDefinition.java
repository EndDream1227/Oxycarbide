package com.kaze943.oxycarbide.shop;

import java.util.ArrayList;
import java.util.List;

/**
 * One clickable entry of a shop GUI, as written in the shop JSON file.
 */
public final class ShopItemDefinition {
	/** Row and column on the GUI, 1-based. */
	public int row = 1;
	public int column = 1;

	/** Item id shown in the slot; defaults to the traded item when empty. */
	public String display = "";
	public boolean enchanted;

	public String name = "";
	public List<String> lore = new ArrayList<>();

	/** {@code buy}, {@code sell}, {@code redirect}, {@code command} or {@code placeholder}. */
	public String type = "placeholder";

	/** Traded item id, used by buy/sell. */
	public String item = "";
	/** Quantity per trade. */
	public int amount = 1;
	/** Money moved on buy/sell, in the base currency. */
	public long price;
	/** Currency used for this trade: {@code normal} or a special currency name. */
	public String currency = "normal";

	/** Target shop id for {@code redirect}. */
	public String target = "";

	/**
	 * Command run after a successful purchase, for {@code command}. It runs with server
	 * permissions, so normal players can buy access to an operator command; {@code %player%}
	 * is replaced with the buyer's name.
	 */
	public String command = "";

	public String typeLower() {
		return type == null ? "placeholder" : type.toLowerCase(java.util.Locale.ROOT);
	}

	public String displayId() {
		if (display != null && !display.isBlank()) {
			return display;
		}

		return item;
	}
}
