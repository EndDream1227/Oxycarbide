package com.kaze943.oxycarbide.travel;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable {@code /back} messages, loaded from
 * {@code config/oxycarbide/lang/zh/back_text.json} and
 * {@code config/oxycarbide/lang/en/back_text.json}.
 */
public final class BackTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"none", "没有可以返回的位置",
			"teleport", "已传送到先前的位置",
			"dimension_missing", "先前位置所在的维度「%s」不存在");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"none", "There is no position to return to.",
			"teleport", "Teleported back to your previous position.",
			"dimension_missing", "The dimension \"%s\" of your previous position does not exist.");

	private static final JsonTexts TEXTS = new JsonTexts("back_text.json", DEFAULT_ZH, DEFAULT_EN);

	private BackTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
