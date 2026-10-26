package com.kaze943.oxycarbide.shop;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable shop messages, loaded from
 * {@code config/oxycarbide/lang/zh/shop_text.json} and
 * {@code config/oxycarbide/lang/en/shop_text.json}.
 */
public final class ShopTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"buy_success", "已购买 %s 个「%s」，花费 %s %s",
			"buy_insufficient", "余额不足，需要 %s %s",
			"inventory_full", "背包已满，物品已掉落在脚下",
			"sell_success", "已出售 %s 个「%s」，获得 %s %s",
			"sell_missing", "背包中没有足够的「%s」（需要 %s 个）",
			"command_success", "已购买「%s」，相关指令已执行");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"buy_success", "Bought %s \"%s\" for %s %s.",
			"buy_insufficient", "Not enough money, you need %s %s.",
			"inventory_full", "Your inventory is full, the item was dropped at your feet.",
			"sell_success", "Sold %s \"%s\" for %s %s.",
			"sell_missing", "You don't have enough \"%s\" (need %s).",
			"command_success", "Purchased \"%s\" and the command has been run.");

	private static final JsonTexts TEXTS = new JsonTexts("shop_text.json", DEFAULT_ZH, DEFAULT_EN);

	private ShopTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
