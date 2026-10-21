package com.kaze943.oxycarbide.sweep;

import com.kaze943.oxycarbide.text.JsonTexts;

import java.util.Map;

/**
 * Customisable sweep messages, loaded from
 * {@code config/oxycarbide/lang/zh/sweep_text.json} and
 * {@code config/oxycarbide/lang/en/sweep_text.json}.
 */
public final class SweepTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"clear", "已清除 %s 个掉落物！",
			"clear_entity", "已清除 %s 个掉落物（和 %s 个怪物，清除怪物模式）！",
			"countdown", "将在 %s 秒后清扫掉落物",
			"smart", "服务器掉落物过多，已自动清扫 %s 个掉落物！");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"clear", "Cleared %s dropped item(s)!",
			"clear_entity", "Cleared %s dropped item(s) (and %s monster(s), monster mode)!",
			"countdown", "Dropped items will be cleared in %s second(s)",
			"smart", "Too many dropped items; cleared %s automatically!");

	private static final JsonTexts TEXTS = new JsonTexts("sweep_text.json", DEFAULT_ZH, DEFAULT_EN);

	private SweepTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static String text(String key, Object... args) {
		return TEXTS.text(key, args);
	}
}
