package com.kaze943.oxycarbide.warp;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable warp messages, loaded from {@code config/oxycarbide/lang/zh/warp_text.json}
 * and {@code config/oxycarbide/lang/en/warp_text.json}.
 */
public final class WarpTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"set", "已设置地标「%s」于 %s",
			"exists", "已存在名为「%s」的地标",
			"limit", "你最多只能设置 %s 个地标",
			"not_found", "不存在名为「%s」的地标",
			"deleted", "已删除地标「%s」",
			"teleport", "已传送到地标「%s」",
			"dimension_missing", "地标「%s」所在的维度「%s」不存在",
			"renamed", "已将地标「%s」重命名为「%s」",
			"rename_exists", "已存在名为「%s」的地标",
			"name_too_long", "地标名称过长（最多 %s 个字符）",
			"not_owner", "你不是地标「%s」的创建者",
			"create_no_permission", "只有管理员可以创建地标",
			"edit_no_permission", "只有管理员可以重命名地标",
			"delete_no_permission", "只有管理员可以删除地标");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"set", "Warp \"%s\" has been set at %s.",
			"exists", "A warp named \"%s\" already exists.",
			"limit", "You can only set up to %s warps.",
			"not_found", "There is no warp named \"%s\".",
			"deleted", "Warp \"%s\" has been deleted.",
			"teleport", "Teleported to warp \"%s\".",
			"dimension_missing", "Warp \"%s\" is in dimension \"%s\", which does not exist.",
			"renamed", "Warp \"%s\" has been renamed to \"%s\".",
			"rename_exists", "A warp named \"%s\" already exists.",
			"name_too_long", "The warp name is too long (max %s characters).",
			"not_owner", "You are not the creator of warp \"%s\".",
			"create_no_permission", "Only operators can create warps.",
			"edit_no_permission", "Only operators can rename warps.",
			"delete_no_permission", "Only operators can delete warps.");

	private static final JsonTexts TEXTS = new JsonTexts("warp_text.json", DEFAULT_ZH, DEFAULT_EN);

	private WarpTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
