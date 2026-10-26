package com.kaze943.oxycarbide.title;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable title messages, loaded from
 * {@code config/oxycarbide/lang/<language>/title_text.json}.
 */
public final class TitleTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"disabled", "称号功能已关闭",
			"not_found", "找不到称号 %s",
			"created", "已创建称号 %s",
			"create_failed", "创建失败：%s 已存在，或 id 不合法（只能用字母和数字）",
			"invalid_color", "无效的颜色写法：%s",
			"obtained", "你获得了称号 %s",
			"granted", "已将称号 %s 授予 %s",
			"already_owned", "对方已经拥有该称号",
			"worn", "已佩戴称号 %s",
			"not_owned", "你没有这个称号",
			"cleared", "已取消佩戴称号",
			"target_missing", "找不到玩家 %s",
			"current", "当前佩戴的称号：%s",
			"none", "当前没有佩戴称号");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"disabled", "The title feature is disabled.",
			"not_found", "Title %s could not be found.",
			"created", "Created the title %s.",
			"create_failed", "Could not create the title: %s already exists, or the id is not valid (letters and digits only).",
			"invalid_color", "Invalid colour: %s",
			"obtained", "You received the title %s.",
			"granted", "Gave the title %s to %s.",
			"already_owned", "That player already owns this title.",
			"worn", "Now wearing the title %s.",
			"not_owned", "You do not own that title.",
			"cleared", "Title removed.",
			"target_missing", "Player %s could not be found.",
			"current", "Current title: %s",
			"none", "You are not wearing a title.");

	private static final JsonTexts TEXTS = new JsonTexts("title_text.json", DEFAULT_ZH, DEFAULT_EN);

	private TitleTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
