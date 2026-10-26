package com.kaze943.oxycarbide.sidebar;

import java.util.ArrayList;
import java.util.List;

/**
 * The sidebar layout, loaded from {@code config/oxycarbide/scoreboard/scoreboard.json}.
 *
 * <p>Minecraft shows at most 15 lines in the sidebar; extra lines are dropped.</p>
 */
public final class SidebarSettings {
	/** Top title of the sidebar. Supports {@code &} colour codes and placeholders. */
	public String title = "";

	/** How often the sidebar is rebuilt, in ticks. */
	public int updateTicks = 20;

	/** Default number of players for lists; {@code 0} means every online player. */
	public int players = 0;

	/** Default order of lists: {@code desc}, {@code asc} or {@code join}. */
	public String sort = "desc";

	public List<SidebarLine> lines = new ArrayList<>();

	public void normalize() {
		if (title == null) {
			title = "";
		}

		if (lines == null) {
			lines = new ArrayList<>();
		}

		lines.removeIf(line -> line == null);
		updateTicks = Math.max(1, Math.min(600, updateTicks));
		players = Math.max(0, players);

		if (sort == null || sort.isBlank()) {
			sort = "desc";
		}
	}
}
