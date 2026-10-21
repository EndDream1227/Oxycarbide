package com.kaze943.oxycarbide.travel;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable random-teleport messages, loaded from
 * {@code config/oxycarbide/lang/zh/rtp_text.json} and
 * {@code config/oxycarbide/lang/en/rtp_text.json}.
 */
public final class RtpTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"searching", "正在寻找合适的传送点…",
			"countdown", "传送将在 %s 秒后开始",
			"teleport", "已随机传送到 %s",
			"failed", "未能找到合适的位置，请稍后再试",
			"cooldown", "随机传送正在冷却：%s 秒",
			"busy", "多个玩家正在请求随机传送，请稍后再试",
			"cancelled", "随机传送请求已被取消");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"searching", "Looking for a suitable teleport target…",
			"countdown", "Teleporting in %s second(s).",
			"teleport", "Randomly teleported to %s.",
			"failed", "Could not find a suitable place, please try again later.",
			"cooldown", "Random teleport is cooling down: %s second(s).",
			"busy", "Several players are requesting a random teleport, please try again later.",
			"cancelled", "Your random teleport request has been cancelled.");

	private static final JsonTexts TEXTS = new JsonTexts("rtp_text.json", DEFAULT_ZH, DEFAULT_EN);

	private RtpTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
