package com.kaze943.oxycarbide.home;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable home messages, loaded from {@code config/oxycarbide/lang/zh/home_text.json}
 * and {@code config/oxycarbide/lang/en/home_text.json}.
 */
public final class HomeTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"set", "已将家「%s」设置在 %s",
			"exists", "你已经有名为「%s」的家了",
			"limit", "你最多只能拥有 %s 个家",
			"not_found", "你还没有名为「%s」的家",
			"deleted", "已删除家「%s」",
			"teleport", "已传送到家「%s」",
			"dimension_missing", "家「%s」所在的维度「%s」不存在",
			"name_too_long", "家名称过长（最多 %s 个字符）");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"set", "Home \"%s\" has been set at %s.",
			"exists", "You already have a home named \"%s\".",
			"limit", "You can only have up to %s homes.",
			"not_found", "You don't have a home named \"%s\".",
			"deleted", "Home \"%s\" has been deleted.",
			"teleport", "Teleported to home \"%s\".",
			"dimension_missing", "Home \"%s\" is in dimension \"%s\", which does not exist.",
			"name_too_long", "The home name is too long (max %s characters).");

	private static final JsonTexts TEXTS = new JsonTexts("home_text.json", DEFAULT_ZH, DEFAULT_EN);

	private HomeTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
