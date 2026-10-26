package com.kaze943.oxycarbide.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One clickable entry of a menu, as written in the menu JSON file.
 */
public final class MenuButton {
	/** Row and column on the menu, 1-based. */
	public int row = 1;
	public int column = 1;

	/** Item id shown in the slot. */
	public String item = "minecraft:stone";
	public boolean enchanted;

	public String name = "";
	public List<String> lore = new ArrayList<>();

	/**
	 * What a click does: {@code command} runs {@link #value} as the clicking player,
	 * {@code menu} opens the menu named {@link #value}, {@code placeholder} does nothing.
	 */
	public String action = "placeholder";

	/** Command text (a leading slash is optional) or the target menu id. */
	public String value = "";

	public String actionLower() {
		return action == null ? "placeholder" : action.toLowerCase(Locale.ROOT);
	}
}
