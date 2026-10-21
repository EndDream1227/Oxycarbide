package com.kaze943.oxycarbide.treasury;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable special-currency transfer messages, loaded from
 * {@code config/oxycarbide/lang/zh/sptreasury_text.json} and
 * {@code config/oxycarbide/lang/en/sptreasury_text.json}.
 */
public final class SpecialTreasuryTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"insufficient", "%s 的「%s」余额不足（当前 %s）",
			"transfer_disabled", "服务器未允许玩家之间转账特殊货币",
			"self", "不能向自己转账",
			"transferred", "已向 %s 转账 %s 个「%s」",
			"received", "收到来自 %s 的 %s 个「%s」");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"insufficient", "%s does not have enough \"%s\" (current: %s).",
			"transfer_disabled", "Player-to-player special currency transfers are disabled.",
			"self", "You cannot transfer money to yourself.",
			"transferred", "Transferred to %s: %s %s.",
			"received", "Received from %s: %s %s.");

	private static final JsonTexts TEXTS = new JsonTexts("sptreasury_text.json", DEFAULT_ZH, DEFAULT_EN);

	private SpecialTreasuryTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
