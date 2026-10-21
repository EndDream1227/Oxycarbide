package com.kaze943.oxycarbide.shop;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.MenuType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds shop menus, renders their display items and performs the trades.
 */
public final class ShopService {
	private static final ShopService INSTANCE = new ShopService();

	private ShopService() {
	}

	public static ShopService get() {
		return INSTANCE;
	}

	public void open(ServerPlayer player, String id) {
		ShopDefinition shop = ShopManager.get().get(id);

		if (shop == null) {
			player.sendSystemMessage(Component.translatable("oxycarbide.shop.not_found", id));
			return;
		}

		open(player, shop);
	}

	public void open(ServerPlayer player, ShopDefinition shop) {
		int rows = Math.max(1, Math.min(6, shop.rows));
		int size = rows * 9;
		SimpleContainer display = new SimpleContainer(size);

		for (ShopItemDefinition item : shop.items) {
			int slot = ShopDefinition.slotOf(item);

			if (slot >= 0 && slot < size) {
				display.setItem(slot, buildStack(item));
			}
		}

		player.openMenu(new SimpleMenuProvider(
				(containerId, playerInventory, opener) -> new ShopMenu(containerId, playerInventory, shop,
						display, rows, menuType(rows)),
				Component.literal(shop.title)));
	}

	private static MenuType<?> menuType(int rows) {
		return switch (rows) {
			case 1 -> MenuType.GENERIC_9x1;
			case 2 -> MenuType.GENERIC_9x2;
			case 3 -> MenuType.GENERIC_9x3;
			case 4 -> MenuType.GENERIC_9x4;
			case 5 -> MenuType.GENERIC_9x5;
			default -> MenuType.GENERIC_9x6;
		};
	}

	private ItemStack buildStack(ShopItemDefinition definition) {
		ItemStack stack = new ItemStack(itemOf(definition.displayId()));

		if (definition.name != null && !definition.name.isBlank()) {
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(definition.name));
		}

		List<Component> lore = new ArrayList<>();

		if (definition.lore != null) {
			for (String line : definition.lore) {
				lore.add(Component.literal(line));
			}
		}

		String type = definition.typeLower();

		if (("buy".equals(type) || "sell".equals(type)) && definition.price > 0) {
			lore.add(Component.translatable("oxycarbide.shop.price",
					definition.price, com.kaze943.oxycarbide.config.OxycarbideConfig.get().getCurrencyName()));
		}

		if (!lore.isEmpty()) {
			stack.set(DataComponents.LORE, new ItemLore(lore));
		}

		if (definition.enchanted) {
			stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}

		return stack;
	}

	/** Called from {@link ShopMenu} for left clicks on a shop slot. */
	public void handleClick(ServerPlayer player, ShopDefinition shop, int slot) {
		ShopItemDefinition definition = shop.atSlot(slot);

		if (definition == null) {
			return;
		}

		switch (definition.typeLower()) {
			case "buy" -> buy(player, definition);
			case "sell" -> sell(player, definition);
			case "redirect" -> redirect(player, definition);
			default -> {
				// placeholder: no reaction
			}
		}
	}

	private void buy(ServerPlayer player, ShopItemDefinition definition) {
		String currency = currencyOf(definition);

		if (!ensureCurrency(player, currency)) {
			return;
		}

		long price = Math.max(0L, definition.price);

		if (balanceOf(player, currency) < price) {
			player.sendSystemMessage(ShopTexts.component("buy_insufficient",
					price, labelOf(currency)));
			return;
		}

		takeMoney(player, currency, price);

		ItemStack stack = new ItemStack(itemOf(definition.item), Math.max(1, definition.amount));

		if (!player.getInventory().add(stack)) {
			player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
			player.sendSystemMessage(ShopTexts.component("inventory_full"));
		}

		player.sendSystemMessage(ShopTexts.component("buy_success",
				definition.amount, label(definition), price, labelOf(currency)));
	}

	private void sell(ServerPlayer player, ShopItemDefinition definition) {
		String currency = currencyOf(definition);

		if (!ensureCurrency(player, currency)) {
			return;
		}

		Item item = itemOf(definition.item);
		int amount = Math.max(1, definition.amount);

		if (count(player, item) < amount) {
			player.sendSystemMessage(ShopTexts.component("sell_missing", label(definition), amount));
			return;
		}

		remove(player, item, amount);

		long price = Math.max(0L, definition.price);
		addMoney(player, currency, price);

		player.sendSystemMessage(ShopTexts.component("sell_success",
				amount, label(definition), price, labelOf(currency)));
	}

	private static String currencyOf(ShopItemDefinition definition) {
		if (definition.currency == null || definition.currency.isBlank()) {
			return "normal";
		}

		return definition.currency;
	}

	private String labelOf(String currency) {
		if (currency == null || "normal".equalsIgnoreCase(currency)) {
			return com.kaze943.oxycarbide.config.OxycarbideConfig.get().getCurrencyName();
		}

		return currency;
	}

	private boolean ensureCurrency(ServerPlayer player, String currency) {
		if (currency == null || "normal".equalsIgnoreCase(currency)) {
			if (!com.kaze943.oxycarbide.config.OxycarbideConfig.get().isTreasuryEnabled()) {
				player.sendSystemMessage(Component.translatable("oxycarbide.shop.treasury_disabled"));
				return false;
			}

			return true;
		}

		if (!com.kaze943.oxycarbide.treasury.SpecialCurrencyManager.get().exists(currency)) {
			player.sendSystemMessage(Component.translatable("oxycarbide.shop.currency_missing", currency));
			return false;
		}

		return true;
	}

	private long balanceOf(ServerPlayer player, String currency) {
		if (currency == null || "normal".equalsIgnoreCase(currency)) {
			return com.kaze943.oxycarbide.treasury.TreasuryManager.get().balance(player.getUUID());
		}

		return com.kaze943.oxycarbide.treasury.SpecialCurrencyManager.get().balance(currency, player.getUUID());
	}

	private void takeMoney(ServerPlayer player, String currency, long price) {
		if (currency == null || "normal".equalsIgnoreCase(currency)) {
			com.kaze943.oxycarbide.treasury.TreasuryManager.get()
					.forceCut(player.getUUID(), player.getScoreboardName(), price);
			return;
		}

		com.kaze943.oxycarbide.treasury.SpecialCurrencyManager.get()
				.forceCut(currency, player.getUUID(), player.getScoreboardName(), price);
	}

	private void addMoney(ServerPlayer player, String currency, long price) {
		if (currency == null || "normal".equalsIgnoreCase(currency)) {
			com.kaze943.oxycarbide.treasury.TreasuryManager.get()
					.add(player.getUUID(), player.getScoreboardName(), price);
			return;
		}

		com.kaze943.oxycarbide.treasury.SpecialCurrencyManager.get()
				.add(currency, player.getUUID(), player.getScoreboardName(), price);
	}

	private void redirect(ServerPlayer player, ShopItemDefinition definition) {
		String target = definition.target;

		if (target == null || !ShopManager.get().contains(target)) {
			player.sendSystemMessage(Component.translatable("oxycarbide.shop.redirect_missing", target));
			return;
		}

		player.closeContainer();
		open(player, ShopManager.get().get(target));
	}

	private String label(ShopItemDefinition definition) {
		if (definition.name != null && !definition.name.isBlank()) {
			return definition.name;
		}

		return definition.item;
	}

	private static Item itemOf(String id) {
		if (id == null || id.isBlank()) {
			return Items.STONE;
		}

		Identifier identifier = Identifier.tryParse(id);
		Item item = identifier == null ? null : BuiltInRegistries.ITEM.getValue(identifier);
		return item == null ? Items.STONE : item;
	}

	private static int count(ServerPlayer player, Item item) {
		int count = 0;

		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);

			if (!stack.isEmpty() && stack.getItem() == item) {
				count += stack.getCount();
			}
		}

		return count;
	}

	private static void remove(ServerPlayer player, Item item, int amount) {
		int left = amount;

		for (int i = 0; i < player.getInventory().getContainerSize() && left > 0; i++) {
			ItemStack stack = player.getInventory().getItem(i);

			if (stack.isEmpty() || stack.getItem() != item) {
				continue;
			}

			int take = Math.min(left, stack.getCount());
			stack.shrink(take);
			left -= take;
		}
	}
}
