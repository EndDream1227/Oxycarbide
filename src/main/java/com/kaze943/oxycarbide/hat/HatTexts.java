package com.kaze943.oxycarbide.hat;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable hat messages, loaded from {@code config/oxycarbide/lang/zh/hat_text.json}
 * and {@code config/oxycarbide/lang/en/hat_text.json}.
 */
public final class HatTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"empty", "你手中没有物品",
			"success", "已将「%s」戴在头上");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"empty", "You are not holding any item.",
			"success", "Put \"%s\" on your head.");

	private static final JsonTexts TEXTS = new JsonTexts("hat_text.json", DEFAULT_ZH, DEFAULT_EN);

	private HatTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
