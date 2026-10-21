package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.treasury.SpecialTreasuryTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * {@code /sptreasury create}, {@code /add_sptreasury}, {@code /cut_sptreasury} and
 * {@code /transfer_sptreasury}.
 */
public final class SpecialTreasuryCommands {
	private SpecialTreasuryCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("sptreasury")
					.executes(SpecialTreasuryCommands::list)
					.then(Commands.literal("create")
							.requires(Admins::isAdmin)
							.then(Commands.argument("currency", StringArgumentType.word())
									.then(Commands.argument("rate", DoubleArgumentType.doubleArg(0.0))
											.executes(SpecialTreasuryCommands::create)))));

			dispatcher.register(Commands.literal("add_sptreasury")
					.requires(Admins::isAdmin)
					.then(Commands.argument("currency", StringArgumentType.word())
							.then(Commands.argument("player", StringArgumentType.word())
									.then(Commands.argument("amount", LongArgumentType.longArg(1))
											.executes(SpecialTreasuryCommands::add)))));

			dispatcher.register(Commands.literal("cut_sptreasury")
					.requires(Admins::isAdmin)
					.then(Commands.argument("currency", StringArgumentType.word())
							.then(Commands.argument("player", StringArgumentType.word())
									.then(Commands.argument("amount", LongArgumentType.longArg(1))
											.executes(SpecialTreasuryCommands::cut)))));

			dispatcher.register(Commands.literal("transfer_sptreasury")
					.then(Commands.argument("currency", StringArgumentType.word())
							.then(Commands.argument("player", StringArgumentType.word())
									.then(Commands.argument("amount", LongArgumentType.longArg(1))
											.executes(SpecialTreasuryCommands::transfer)))));
		});
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		List<SpecialCurrencyManager.Currency> currencies = SpecialCurrencyManager.get().currencies();

		if (currencies.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.sp.list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.sp.list_header", currencies.size()), false);

		for (SpecialCurrencyManager.Currency currency : currencies) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.sp.list_entry",
					currency.name, currency.rate), false);
		}

		return currencies.size();
	}

	private static int create(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "currency");
		double rate = DoubleArgumentType.getDouble(context, "rate");
		SpecialCurrencyManager manager = SpecialCurrencyManager.get();

		if (!manager.create(name, rate)) {
			source.sendFailure(Component.translatable("oxycarbide.sp.currency_exists", name));
			return 0;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.sp.currency_created", name, rate), true);
		return 1;
	}

	private static int add(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "currency");

		if (!ensureCurrency(source, name)) {
			return 0;
		}

		String playerName = StringArgumentType.getString(context, "player");
		long amount = LongArgumentType.getLong(context, "amount");
		UUID target = resolve(source, name, playerName);

		if (target == null) {
			return 0;
		}

		SpecialCurrencyManager.get().add(name, target, playerName, amount);
		source.sendSuccess(() -> Component.translatable("oxycarbide.sp.added", playerName, amount, name), true);
		return 1;
	}

	private static int cut(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "currency");

		if (!ensureCurrency(source, name)) {
			return 0;
		}

		String playerName = StringArgumentType.getString(context, "player");
		long amount = LongArgumentType.getLong(context, "amount");
		UUID target = resolve(source, name, playerName);

		if (target == null) {
			return 0;
		}

		long newBalance = SpecialCurrencyManager.get().forceCut(name, target, playerName, amount);

		if (newBalance < 0) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.sp.cut_negative",
					playerName, name, newBalance), true);
		} else {
			source.sendSuccess(() -> Component.translatable("oxycarbide.sp.cut", playerName, amount, name), true);
		}

		return 1;
	}

	private static int transfer(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		if (!OxycarbideConfig.get().isAllowPlayerTranSPTreasury()) {
			source.sendFailure(SpecialTreasuryTexts.component("transfer_disabled"));
			return 0;
		}

		String name = StringArgumentType.getString(context, "currency");

		if (!ensureCurrency(source, name)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String playerName = StringArgumentType.getString(context, "player");
		long amount = LongArgumentType.getLong(context, "amount");

		if (playerName.equalsIgnoreCase(player.getScoreboardName())) {
			source.sendFailure(SpecialTreasuryTexts.component("self"));
			return 0;
		}

		UUID target = resolve(source, name, playerName);

		if (target == null) {
			return 0;
		}

		SpecialCurrencyManager manager = SpecialCurrencyManager.get();
		long balance = manager.balance(name, player.getUUID());

		if (balance < amount) {
			source.sendFailure(SpecialTreasuryTexts.component("insufficient",
					player.getScoreboardName(), name, balance));
			return 0;
		}

		manager.transfer(name, player.getUUID(), target, playerName, amount);
		source.sendSuccess(() -> SpecialTreasuryTexts.component("transferred",
				playerName, amount, name), false);

		ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(target);

		if (targetPlayer != null) {
			targetPlayer.sendSystemMessage(SpecialTreasuryTexts.component("received",
					player.getScoreboardName(), amount, name));
		}

		return 1;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isAllowSpecialCurrency()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.sp.disabled"));
		return false;
	}

	private static boolean ensureCurrency(CommandSourceStack source, String name) {
		if (SpecialCurrencyManager.get().exists(name)) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.sp.currency_not_found", name));
		return false;
	}

	/** Resolves an online player first, then a stored offline holder of that currency. */
	private static UUID resolve(CommandSourceStack source, String currency, String playerName) {
		ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(playerName);

		if (online != null) {
			return online.getUUID();
		}

		UUID stored = SpecialCurrencyManager.get().findByName(currency, playerName);

		if (stored == null) {
			source.sendFailure(Component.translatable("oxycarbide.sp.player_not_found", playerName));
			return null;
		}

		return stored;
	}
}
