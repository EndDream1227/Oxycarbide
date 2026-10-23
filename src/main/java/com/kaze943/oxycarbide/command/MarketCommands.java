package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.market.MarketListing;
import com.kaze943.oxycarbide.market.MarketManager;
import com.kaze943.oxycarbide.market.MarketService;
import com.kaze943.oxycarbide.market.MarketTexts;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * {@code /globalshop}, {@code /sell}, {@code /pull} and {@code /spull}.
 */
public final class MarketCommands {
	private MarketCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("globalshop")
					.executes(MarketCommands::open));

			dispatcher.register(Commands.literal("sell")
					.then(Commands.argument("currency", StringArgumentType.word())
							.suggests(Suggestions.CURRENCIES)
							.then(Commands.argument("price", LongArgumentType.longArg(1))
									.then(Commands.argument("time", StringArgumentType.word())
											.executes(MarketCommands::sell)))));

			dispatcher.register(Commands.literal("pull")
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(Suggestions.MARKET_IDS)
							.executes(MarketCommands::pull)));

			dispatcher.register(Commands.literal("spull")
					.requires(Admins::isAdmin)
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(Suggestions.MARKET_IDS)
							.executes(MarketCommands::forcePull)));
		});
	}

	private static int open(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		MarketService.get().open(player, 1);
		return 1;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isMarketEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.market.disabled"));
		return false;
	}

	private static int sell(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		ItemStack held = player.getMainHandItem();

		if (held.isEmpty()) {
			source.sendFailure(MarketTexts.component("empty_hand"));
			return 0;
		}

		String currency = StringArgumentType.getString(context, "currency");
		long price = LongArgumentType.getLong(context, "price");

		if (!isNormal(currency) && !SpecialCurrencyManager.get().exists(currency)) {
			source.sendFailure(Component.translatable("oxycarbide.market.currency_missing", currency));
			return 0;
		}

		int minutes;

		try {
			String time = StringArgumentType.getString(context, "time");
			minutes = "~".equals(time)
					? OxycarbideConfig.get().getMarketDefaultMinutes()
					: Integer.parseInt(time);
		} catch (NumberFormatException e) {
			source.sendFailure(Component.translatable("oxycarbide.market.invalid_time"));
			return 0;
		}

		if (minutes <= 0) {
			source.sendFailure(Component.translatable("oxycarbide.market.invalid_time"));
			return 0;
		}

		ItemStack copy = held.copy();
		MarketListing listing = MarketService.get().createListing(player, copy, currency, price, minutes);
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		MarketService.get().sendListingCreated(player, listing, copy, price, currency);
		return 1;
	}

	private static int pull(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		String id = StringArgumentType.getString(context, "id");
		return MarketService.get().pull(player, id) ? 1 : 0;
	}

	private static int forcePull(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String id = StringArgumentType.getString(context, "id");
		MarketListing listing = MarketService.get().forcePull(id);

		if (listing == null) {
			source.sendFailure(Component.translatable("oxycarbide.market.not_found", id));
			return 0;
		}

		MarketService.get().refund(listing, source.getServer());
		source.sendSuccess(() -> Component.translatable("oxycarbide.market.spulled", id), true);
		return 1;
	}

	private static boolean isNormal(String currency) {
		return currency == null || currency.isBlank() || "normal".equalsIgnoreCase(currency);
	}
}
