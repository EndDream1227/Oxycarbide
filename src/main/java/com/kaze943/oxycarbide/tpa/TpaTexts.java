package com.kaze943.oxycarbide.tpa;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable teleport-request messages, loaded from
 * {@code config/oxycarbide/lang/zh/tpa_text.json} and
 * {@code config/oxycarbide/lang/en/tpa_text.json}.
 */
public final class TpaTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"self", "你不能向自己发送传送请求",
			"target_not_found", "找不到玩家 %s",
			"target_busy", "对方的待处理传送请求过多，请稍后再试",
			"request_sent", "已向 %s 发送传送请求",
			"incoming", "%s 正请求传送到你的位置，%s %s",
			"incoming_here", "%s 正请求你传送到他的位置，%s %s",
			"auto_deny", "请求将在 %s 秒后自动拒绝",
			"accepted", "对方同意了你的请求",
			"denied", "对方拒绝了你的请求",
			"accepted_self", "你同意了该传送请求",
			"denied_self", "你拒绝了该传送请求",
			"timeout", "传送请求已超时，已自动拒绝",
			"countdown", "传送将会在 %s 秒后开始",
			"cooldown", "传送请求正在冷却：%s 秒",
			"target_offline", "对方已离线，传送请求已取消",
			"teleported", "已传送到 %s 的位置");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"self", "You cannot send a teleport request to yourself.",
			"target_not_found", "Player %s could not be found.",
			"target_busy", "That player has too many pending teleport requests, please try again later.",
			"request_sent", "Teleport request sent to %s.",
			"incoming", "%s wants to teleport to you, %s %s",
			"incoming_here", "%s wants you to teleport to them, %s %s",
			"auto_deny", "The request will be denied automatically in %s second(s).",
			"accepted", "The other player accepted your request.",
			"denied", "The other player denied your request.",
			"accepted_self", "You accepted the teleport request.",
			"denied_self", "You denied the teleport request.",
			"timeout", "The teleport request timed out and was denied automatically.",
			"countdown", "Teleporting in %s second(s).",
			"cooldown", "Teleport requests are cooling down: %s second(s).",
			"target_offline", "The other player went offline, the teleport was cancelled.",
			"teleported", "Teleported to %s.");

	private static final JsonTexts TEXTS = new JsonTexts("tpa_text.json", DEFAULT_ZH, DEFAULT_EN);

	private TpaTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
