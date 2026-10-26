package com.kaze943.oxycarbide.title;

import java.util.Locale;

/**
 * One title, as written in {@code config/oxycarbide/title/titles.json}.
 *
 * <p>Every title needs three things: an {@link #id} (letters and digits), the
 * {@link #text} shown in front of the player name and a {@link #color} description.</p>
 */
public final class TitleDefinition {
	public String id = "";
	public String text = "";
	/** Named colour ({@code gold}), {@code #rrggbb} / {@code 0xrrggbb} or a gradient. */
	public String color = "white";

	public String idLower() {
		return id == null ? "" : id.toLowerCase(Locale.ROOT);
	}
}
