package com.kaze943.oxycarbide.nickname;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable nickname messages, loaded from
 * {@code config/oxycarbide/lang/zh/nickname_text.json} and
 * {@code config/oxycarbide/lang/en/nickname_text.json}.
 */
public final class NicknameTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"set", "已将聊天昵称设置为「%s」",
			"set_other", "已将 %s 的昵称设置为「%s」",
			"received", "你的昵称已被设置为「%s」",
			"invalid", "昵称长度必须在 1 到 %s 个字符之间",
			"target_not_found", "找不到玩家 %s");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"set", "Your chat nickname is now \"%s\".",
			"set_other", "Set the nickname of %s to \"%s\".",
			"received", "Your nickname has been set to \"%s\".",
			"invalid", "The nickname must be between 1 and %s characters.",
			"target_not_found", "Player %s could not be found.");

	private static final JsonTexts TEXTS = new JsonTexts("nickname_text.json", DEFAULT_ZH, DEFAULT_EN);

	private NicknameTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
