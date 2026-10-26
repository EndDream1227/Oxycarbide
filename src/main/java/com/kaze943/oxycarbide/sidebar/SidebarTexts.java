package com.kaze943.oxycarbide.sidebar;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable sidebar messages, loaded from
 * {@code config/oxycarbide/lang/<language>/sidebar_text.json}.
 *
 * <p>{@code occupied} is shown to whoever runs a vanilla sidebar command while the
 * oxycarbide sidebar is on, so it is a config text and not a translation key: that way it
 * also reads correctly for players who do not have the mod installed.</p>
 */
public final class SidebarTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"occupied", "Oxycarbide正在占用侧边栏，请关闭Oxycarbide记分板侧边栏后再试!",
			"reloaded", "已重新加载侧边栏布局：%s 行",
			"target_missing", "找不到玩家 %s",
			"counter_set", "计数器 %s（玩家 %s）已设为 %s",
			"counter_added", "计数器 %s（玩家 %s）已调整为 %s");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"occupied", "Oxycarbide is using the sidebar right now. Turn the Oxycarbide scoreboard sidebar off and try again!",
			"reloaded", "Sidebar layout reloaded: %s line(s).",
			"target_missing", "Player %s could not be found.",
			"counter_set", "Set counter %s of %s to %s.",
			"counter_added", "Counter %s of %s is now %s.");

	private static final JsonTexts TEXTS = new JsonTexts("sidebar_text.json", DEFAULT_ZH, DEFAULT_EN);

	private SidebarTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
