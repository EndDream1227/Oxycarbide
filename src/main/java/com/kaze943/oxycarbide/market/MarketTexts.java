package com.kaze943.oxycarbide.market;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable global-market messages, loaded from
 * {@code config/oxycarbide/lang/zh/market_text.json} and
 * {@code config/oxycarbide/lang/en/market_text.json}.
 */
public final class MarketTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"sell_success", "您的%s已成功上架，售价%s%s，交易id为:%s",
			"empty_hand", "你手中没有物品",
			"pulled", "已撤回交易 %s",
			"success", "交易成功",
			"bought", "%s购买了你的商品，你获得了%s%s",
			"race", "无法交易：商品已被他人购买",
			"expired", "该交易已过期，已从市场移除",
			"insufficient", "余额不足，需要 %s %s",
			"inventory_full", "背包已满，物品已掉落在脚下",
			"pending_delivered", "已取回市场返还的 %s 件物品");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"sell_success", "Listed %s for %s %s, trade id: %s.",
			"empty_hand", "You are not holding any item.",
			"pulled", "Listing %s has been withdrawn.",
			"success", "Trade successful.",
			"bought", "%s bought your item, you received %s %s.",
			"race", "Cannot trade: the item was already bought by someone else.",
			"expired", "This listing has expired and was removed.",
			"insufficient", "Not enough money, you need %s %s.",
			"inventory_full", "Your inventory is full, the item was dropped at your feet.",
			"pending_delivered", "Received %s returned market item(s).");

	private static final JsonTexts TEXTS = new JsonTexts("market_text.json", DEFAULT_ZH, DEFAULT_EN);

	private MarketTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
