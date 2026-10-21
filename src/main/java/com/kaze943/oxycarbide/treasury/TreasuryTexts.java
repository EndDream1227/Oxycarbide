package com.kaze943.oxycarbide.treasury;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable treasury messages, loaded from
 * {@code config/oxycarbide/lang/zh/treasury_text.json} and
 * {@code config/oxycarbide/lang/en/treasury_text.json}.
 */
public final class TreasuryTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"account", "账户：%s，你的账户上有%s余额：%s",
			"account_currency", "，%s余额：%s",
			"player_not_found", "找不到玩家 %s",
			"added", "已为 %s 添加 %s %s",
			"cut", "已从 %s 的账户扣除 %s %s",
			"insufficient", "%s 的余额不足（当前 %s）",
			"transfer_disabled", "服务器未允许玩家之间转账",
			"self", "不能向自己转账",
			"limit", "单次转账不能超过 %s %s",
			"transferred", "已向 %s 转账 %s %s",
			"received", "收到来自 %s 的 %s %s");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"account", "Account %s: your %s balance is %s",
			"account_currency", ", %s balance: %s",
			"player_not_found", "Player %s could not be found.",
			"added", "Added to %s: %s %s.",
			"cut", "Deducted from %s: %s %s.",
			"insufficient", "%s does not have enough money (current: %s).",
			"transfer_disabled", "Player-to-player transfers are disabled on this server.",
			"self", "You cannot transfer money to yourself.",
			"limit", "A single transfer cannot exceed %s %s.",
			"transferred", "Transferred to %s: %s %s.",
			"received", "Received from %s: %s %s.");

	private static final JsonTexts TEXTS = new JsonTexts("treasury_text.json", DEFAULT_ZH, DEFAULT_EN);

	private TreasuryTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
