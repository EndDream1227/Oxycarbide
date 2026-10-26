package com.kaze943.oxycarbide.sidebar;

import java.util.Locale;

/**
 * One line of the sidebar, as written in {@code scoreboard.json}.
 *
 * <p>The text kinds {@code title}, {@code bold} and {@code text} always produce exactly
 * one line. The score kinds {@code money}, {@code objective}, {@code stat} and
 * {@code custom} produce a ranked list, or a single line when {@link #self} is set.</p>
 */
public final class SidebarLine {
	/** {@code text}, {@code bold}, {@code title}, {@code money}, {@code objective}, {@code stat} or {@code custom}. */
	public String type = "text";

	/** Template of a text line; placeholders and {@code &} colour codes are supported. */
	public String text = "";

	/** Objective name for {@code objective}. */
	public String objective = "";
	/** Statistic id such as {@code minecraft:deaths} for {@code stat}. */
	public String stat = "";
	/** Counter name for {@code custom}, managed by {@code /oxycarbide scoreboard counter}. */
	public String counter = "";

	/** Only show the viewing player's own value instead of a ranked list. */
	public boolean self;

	/** {@code desc}, {@code asc} or {@code join}; empty inherits the global setting. */
	public String sort = "";

	/** Players to show; {@code -1} inherits the global setting, {@code 0} means everyone. */
	public int players = -1;

	/** Template of a score line: {@code %rank%}, {@code %player%}, {@code %value%}. */
	public String format = "%player% %value%";

	public String typeLower() {
		return type == null ? "text" : type.toLowerCase(Locale.ROOT).trim();
	}

	public String sortLower() {
		return sort == null ? "" : sort.toLowerCase(Locale.ROOT).trim();
	}
}
