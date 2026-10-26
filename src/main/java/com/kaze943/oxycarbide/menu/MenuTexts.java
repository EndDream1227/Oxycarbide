package com.kaze943.oxycarbide.menu;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable menu messages, loaded from
 * {@code config/oxycarbide/lang/<language>/menu_text.json}.
 */
public final class MenuTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"not_found", "找不到菜单 %s",
			"no_default", "服务器还没有设置默认菜单，管理员可用 /menu_default <id> 设置",
			"created", "已创建空白菜单 %s",
			"create_failed", "创建失败：%s 已存在，或 id 不合法（只能用字母、数字、_ 和 -）",
			"default_set", "已将 %s 设为默认菜单",
			"default_missing", "菜单 %s 不存在",
			"reloaded", "已重新加载 %s 个菜单",
			"list_header", "菜单（%s 个）：",
			"list_entry", "  %s - %s",
			"list_empty", "还没有任何菜单，用 /menu_create 创建一个");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"not_found", "Menu %s could not be found.",
			"no_default", "No default menu is set yet; an operator can pick one with /menu_default <id>.",
			"created", "Created the empty menu %s.",
			"create_failed", "Could not create the menu: %s already exists, or the id is not valid (letters, digits, _ and - only).",
			"default_set", "Menu %s is now the default menu.",
			"default_missing", "Menu %s does not exist.",
			"reloaded", "Reloaded %s menu(s).",
			"list_header", "Menus (%s):",
			"list_entry", "  %s - %s",
			"list_empty", "There are no menus yet; create one with /menu_create.");

	private static final JsonTexts TEXTS = new JsonTexts("menu_text.json", DEFAULT_ZH, DEFAULT_EN);

	private MenuTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
