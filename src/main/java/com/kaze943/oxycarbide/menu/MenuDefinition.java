package com.kaze943.oxycarbide.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * One menu GUI, loaded from {@code config/oxycarbide/menu/<id>.json}.
 */
public final class MenuDefinition {
	public String title = "";
	/** Number of chest rows, 1 to 6. */
	public int rows = 3;
	public List<MenuButton> buttons = new ArrayList<>();

	/** File name without {@code .json}; not written back by Gson. */
	public transient String id = "";

	public int size() {
		return Math.max(1, Math.min(6, rows)) * 9;
	}

	/** @return the button placed on that menu slot, or {@code null} */
	public MenuButton atSlot(int slot) {
		MenuButton result = null;

		for (MenuButton button : buttons) {
			if (slotOf(button) == slot) {
				// Later entries overwrite earlier ones on screen, so clicks use the last.
				result = button;
			}
		}

		return result;
	}

	public static int slotOf(MenuButton button) {
		return (button.row - 1) * 9 + (button.column - 1);
	}
}
