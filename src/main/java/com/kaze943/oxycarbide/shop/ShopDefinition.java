package com.kaze943.oxycarbide.shop;

import java.util.ArrayList;
import java.util.List;

/**
 * One shop GUI, loaded from {@code config/oxycarbide/shop/config/<id>.json}.
 */
public final class ShopDefinition {
	public String title = "";
	/** Number of chest rows, 1 to 6. */
	public int rows = 6;
	public List<ShopItemDefinition> items = new ArrayList<>();

	/** File name without {@code .json}; not written back by Gson. */
	public transient String id = "";

	public int size() {
		return Math.max(1, Math.min(6, rows)) * 9;
	}

	/** @return the entry placed on that menu slot, or {@code null} */
	public ShopItemDefinition atSlot(int slot) {
		ShopItemDefinition result = null;

		for (ShopItemDefinition item : items) {
			if (slotOf(item) == slot) {
				// Later entries overwrite earlier ones on screen, so clicks use the last.
				result = item;
			}
		}

		return result;
	}

	public static int slotOf(ShopItemDefinition item) {
		return (item.row - 1) * 9 + (item.column - 1);
	}
}
