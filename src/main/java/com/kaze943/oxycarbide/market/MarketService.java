package com.kaze943.oxycarbide.market;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.shop.ShopDefinition;
import com.kaze943.oxycarbide.shop.ShopItemDefinition;
import com.kaze943.oxycarbide.shop.ShopManager;
import com.kaze943.oxycarbide.shop.ShopService;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.treasury.TreasuryManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Renders the global market GUI, performs purchases and manages listings.
 */
public final class MarketService {
	private static final MarketService INSTANCE = new MarketService();

	private MarketService() {
	}

	public static MarketService get() {
		return INSTANCE;
	}

	public void open(ServerPlayer player, int page) {
		MarketManager manager = MarketManager.get();
		manager.prune();

		int pages = manager.pageCount();
		int current = Math.max(1, Math.min(pages, page));
		int rows = manager.rows();
		int size = rows * 9;
		SimpleContainer display = new SimpleContainer(size);

		for (ShopItemDefinition item : manager.layout().frame) {
			int slot = ShopDefinition.slotOf(item);

			if (slot < 0 || slot >= size || slot == manager.prevSlot() || slot == manager.nextSlot()) {
				continue;
			}

			if (slot >= 9 && slot < (rows - 1) * 9) {
				continue;
			}

			display.setItem(slot, displayStack(item));
		}

		display.setItem(manager.prevSlot(), displayStack(manager.layout().previous));
		display.setItem(manager.nextSlot(), displayStack(manager.layout().next));

		List<MarketListing> listings = manager.page(current);
		int first = manager.firstListingSlot();

		for (int i = 0; i < listings.size() && i < manager.listingSlots(); i++) {
			display.setItem(first + i, listingStack(listings.get(i), player));
		}

		player.openMenu(new SimpleMenuProvider(
				(containerId, playerInventory, opener) -> new MarketMenu(containerId, playerInventory,
						display, rows, menuType(rows), current),
				Component.literal(manager.layout().title)));
	}

	public void click(ServerPlayer player, int page, int slot) {
		MarketManager manager = MarketManager.get();
		manager.prune();

		if (slot == manager.prevSlot()) {
			if (page > 1) {
				reopen(player, page - 1);
			}

			return;
		}

		if (slot == manager.nextSlot()) {
			if (page < manager.pageCount()) {
				reopen(player, page + 1);
			}

			return;
		}

		for (ShopItemDefinition item : manager.layout().frame) {
			if (ShopDefinition.slotOf(item) != slot) {
				continue;
			}

			if ("redirect".equals(item.typeLower()) && ShopManager.get().contains(item.target)) {
				player.closeContainer();
				ShopService.get().open(player, item.target);
			}

			return;
		}

		int first = manager.firstListingSlot();

		if (slot < first || slot >= first + manager.listingSlots()) {
			return;
		}

		int index = (page - 1) * manager.listingSlots() + (slot - first);
		List<MarketListing> listings = manager.page(page);

		if (index >= listings.size()) {
			return;
		}

		MarketListing listing = listings.get(index);

		if (manager.isExpired(listing)) {
			manager.remove(listing.id);
			player.sendSystemMessage(MarketTexts.component("expired"));
			reopen(player, page);
			return;
		}

		if (purchase(player, listing)) {
			reopen(player, page);
		}
	}

	private void reopen(ServerPlayer player, int page) {
		player.closeContainer();
		open(player, page);
	}

	private boolean purchase(ServerPlayer buyer, MarketListing listing) {
		MarketManager manager = MarketManager.get();
		long price = Math.max(0L, listing.price);
		String currency = listing.currency == null ? "normal" : listing.currency;

		if (!hasMoney(buyer, currency, price)) {
			buyer.sendSystemMessage(MarketTexts.component("insufficient",
					price, MarketManager.currencyLabel(currency)));
			return false;
		}

		ItemStack item = decode(listing.item, buyer.level().getServer());

		if (item.isEmpty()) {
			buyer.sendSystemMessage(MarketTexts.component("race"));
			return false;
		}

		takeMoney(buyer, currency, price);

		if (listing.seller != null && !listing.seller.isBlank()) {
			try {
				addMoney(UUID.fromString(listing.seller), listing.sellerName, currency, price);
			} catch (IllegalArgumentException e) {
				// Invalid stored uuid: the seller cannot be credited.
			}
		}

		manager.remove(listing.id);

		if (!buyer.getInventory().add(item)) {
			buyer.drop(item, false, Prediction.SERVER_ONLY);
		}

		buyer.sendSystemMessage(MarketTexts.component("success"));

		if (buyer.level().getServer() != null && listing.seller != null) {
			try {
				ServerPlayer seller = buyer.level().getServer().getPlayerList()
						.getPlayer(UUID.fromString(listing.seller));

				if (seller != null) {
					seller.sendSystemMessage(MarketTexts.component("bought",
							buyer.getScoreboardName(), price, MarketManager.currencyLabel(currency)));
				}
			} catch (IllegalArgumentException e) {
				// Invalid stored uuid.
			}
		}

		return true;
	}

	/** Creates a listing from a copy of the held stack; the caller clears the hand. */
	public MarketListing createListing(ServerPlayer player, ItemStack stack, String currency,
			long price, int minutes) {
		MarketManager manager = MarketManager.get();
		MarketListing listing = new MarketListing();
		listing.id = manager.newId();
		listing.seller = player.getUUID().toString();
		listing.sellerName = player.getScoreboardName();
		listing.itemName = stack.getHoverName().getString();
		listing.item = encode(stack, player.level().getServer());
		listing.currency = currency == null || currency.isBlank() ? "normal" : currency;
		listing.price = Math.max(0L, price);
		listing.createdAt = System.currentTimeMillis();
		listing.expiresAt = listing.createdAt + Math.max(1, minutes) * 60_000L;
		manager.add(listing);
		return listing;
	}

	public void sendListingCreated(ServerPlayer player, MarketListing listing, ItemStack stack,
			long price, String currency) {
		// ShowItem renders the real item tooltip (icon, name, enchantments, ...).
		Component itemName = stack.getHoverName().copy()
				.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowItem(
						ItemStackTemplate.fromNonEmptyStack(stack))));
		Component id = Component.literal(listing.id)
				.withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(listing.id)));

		player.sendSystemMessage(MarketTexts.component("sell_success",
				itemName, price, MarketManager.currencyLabel(currency), id));
	}

	public boolean pull(ServerPlayer player, String id) {
		MarketListing listing = MarketManager.get().find(id);

		if (listing == null) {
			player.sendSystemMessage(Component.translatable("oxycarbide.market.not_found", id));
			return false;
		}

		if (!player.getUUID().toString().equals(listing.seller)) {
			player.sendSystemMessage(Component.translatable("oxycarbide.market.not_owner"));
			return false;
		}

		MarketManager.get().remove(id);
		ItemStack item = decode(listing.item, player.level().getServer());

		if (!item.isEmpty()) {
			give(player, item);
		}

		player.sendSystemMessage(MarketTexts.component("pulled", id));
		return true;
	}

	/** Force-removes a listing and returns it so its item can be refunded. */
	public MarketListing forcePull(String id) {
		MarketListing listing = MarketManager.get().find(id);

		if (listing == null) {
			return null;
		}

		MarketManager.get().remove(id);
		return listing;
	}

	/** Hands the item back to the seller, or queues it when they are offline. */
	public void refund(MarketListing listing, MinecraftServer server) {
		ItemStack item = decode(listing.item, server);

		if (item.isEmpty()) {
			return;
		}

		if (server != null && listing.seller != null && !listing.seller.isBlank()) {
			try {
				ServerPlayer seller = server.getPlayerList().getPlayer(UUID.fromString(listing.seller));

				if (seller != null) {
					give(seller, item);
					return;
				}
			} catch (IllegalArgumentException e) {
				// Invalid stored uuid: queue the item instead.
			}
		}

		MarketManager.get().addPending(listing.seller, listing.itemName, listing.item);
	}

	/** Delivers items that were queued while the player was offline. */
	public void deliverPending(ServerPlayer player) {
		List<PendingReturn> items = MarketManager.get().takePending(player.getUUID());

		if (items.isEmpty()) {
			return;
		}

		MinecraftServer server = player.level().getServer();

		for (PendingReturn entry : items) {
			ItemStack item = decode(entry.item, server);

			if (!item.isEmpty()) {
				give(player, item);
			}
		}

		player.sendSystemMessage(MarketTexts.component("pending_delivered", items.size()));
	}

	private void give(ServerPlayer player, ItemStack item) {
		if (!player.getInventory().add(item)) {
			player.drop(item, false, Prediction.SERVER_ONLY);
			player.sendSystemMessage(MarketTexts.component("inventory_full"));
		}
	}

	private ItemStack listingStack(MarketListing listing, ServerPlayer viewer) {
		ItemStack stack = decode(listing.item, viewer.level().getServer());

		if (stack.isEmpty()) {
			stack = new ItemStack(Items.BARRIER);
		}

		List<Component> lore = new ArrayList<>();

		if (stack.isEnchanted()) {
			lore.add(Component.translatable("oxycarbide.market.enchanted"));
		}

		lore.add(Component.translatable("oxycarbide.market.price_lore",
				listing.price, MarketManager.currencyLabel(listing.currency)));
		lore.add(Component.translatable("oxycarbide.market.seller_lore", listing.sellerName));
		lore.add(Component.translatable("oxycarbide.market.time_lore", remainingMinutes(listing)));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}

	private ItemStack displayStack(ShopItemDefinition definition) {
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

		if (!lore.isEmpty()) {
			stack.set(DataComponents.LORE, new ItemLore(lore));
		}

		if (definition.enchanted) {
			stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}

		return stack;
	}

	private static long remainingMinutes(MarketListing listing) {
		long left = listing.expiresAt - System.currentTimeMillis();
		return left <= 0 ? 0 : (left + 59_999L) / 60_000L;
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

	private static Item itemOf(String id) {
		if (id == null || id.isBlank()) {
			return Items.STONE;
		}

		Identifier identifier = Identifier.tryParse(id);
		Item item = identifier == null ? null : BuiltInRegistries.ITEM.getValue(identifier);
		return item == null ? Items.STONE : item;
	}

	private JsonElement encode(ItemStack stack, MinecraftServer server) {
		if (server == null) {
			return null;
		}

		RegistryOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		return ItemStack.CODEC.encodeStart(ops, stack).result().orElse(null);
	}

	private ItemStack decode(JsonElement json, MinecraftServer server) {
		if (json == null || server == null) {
			return ItemStack.EMPTY;
		}

		RegistryOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		return ItemStack.CODEC.parse(ops, json).result().orElse(ItemStack.EMPTY);
	}

	private boolean hasMoney(ServerPlayer player, String currency, long price) {
		if (isNormal(currency)) {
			return TreasuryManager.get().balance(player.getUUID()) >= price;
		}

		SpecialCurrencyManager manager = SpecialCurrencyManager.get();
		return manager.exists(currency) && manager.balance(currency, player.getUUID()) >= price;
	}

	private void takeMoney(ServerPlayer player, String currency, long price) {
		if (isNormal(currency)) {
			TreasuryManager.get().forceCut(player.getUUID(), player.getScoreboardName(), price);
			return;
		}

		SpecialCurrencyManager.get().forceCut(currency, player.getUUID(), player.getScoreboardName(), price);
	}

	private void addMoney(UUID target, String name, String currency, long price) {
		if (isNormal(currency)) {
			TreasuryManager.get().add(target, name, price);
			return;
		}

		SpecialCurrencyManager.get().add(currency, target, name, price);
	}

	private static boolean isNormal(String currency) {
		return currency == null || currency.isBlank() || "normal".equalsIgnoreCase(currency);
	}
}
