package com.kaze943.oxycarbide.mute;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable mute messages, loaded from
 * {@code config/oxycarbide/lang/zh/mute_text.json} and
 * {@code config/oxycarbide/lang/en/mute_text.json}.
 */
public final class MuteTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"muted", "已将 %s 禁言 %s 秒",
			"muted_permanent", "已将 %s 永久禁言",
			"unmuted", "已解除 %s 的禁言",
			"not_muted", "%s 当前没有被禁言",
			"received", "你已被禁言，剩余 %s 秒",
			"received_permanent", "你已被永久禁言",
			"lifted", "你的禁言已被解除",
			"blocked", "你已被禁言，剩余 %s 秒",
			"blocked_permanent", "你已被永久禁言，无法发送聊天消息");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"muted", "Muted %s for %s second(s).",
			"muted_permanent", "Permanently muted %s.",
			"unmuted", "Unmuted %s.",
			"not_muted", "%s is not muted.",
			"received", "You have been muted for %s second(s).",
			"received_permanent", "You have been permanently muted.",
			"lifted", "Your mute has been lifted.",
			"blocked", "You are muted for another %s second(s).",
			"blocked_permanent", "You are permanently muted and cannot send chat messages.");

	private static final JsonTexts TEXTS = new JsonTexts("mute_text.json", DEFAULT_ZH, DEFAULT_EN);

	private MuteTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
