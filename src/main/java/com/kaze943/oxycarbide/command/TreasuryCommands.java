package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.treasury.TreasuryManager;
import com.kaze943.oxycarbide.treasury.TreasuryTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * {@code /treasury}, {@code /addtreasury}, {@code /cutreasury},
 * {@code /transfer_treasury} and {@code /listreasury}.
 */
public final class TreasuryCommands {
	private TreasuryCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("treasury")
					.executes(TreasuryCommands::balance));

			dispatcher.register(Commands.literal("addtreasury")
					.requires(Admins::isAdmin)
					.then(Commands.argument("player", StringArgumentType.word())
							.then(Commands.argument("amount", LongArgumentType.longArg(1))
									.executes(TreasuryCommands::add))));

			dispatcher.register(Commands.literal("cutreasury")
					.requires(Admins::isAdmin)
					.then(Commands.argument("player", StringArgumentType.word())
							.then(Commands.argument("amount", LongArgumentType.longArg(1))
									.executes(TreasuryCommands::cut))));

			dispatcher.register(Commands.literal("transfer_treasury")
					.then(Commands.argument("player", StringArgumentType.word())
							.then(Commands.argument("amount", LongArgumentType.longArg(1))
									.executes(TreasuryCommands::transfer))));

			dispatcher.register(Commands.literal("listreasury")
					.requires(Admins::isAdmin)
					.executes(TreasuryCommands::list));
		});
	}

	private static int balance(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String playerName = player.getScoreboardName();
		long coins = TreasuryManager.get().balance(player.getUUID());
		String currencyName = OxycarbideConfig.get().getCurrencyName();

		MutableComponent message = Component.empty()
				.append(TreasuryTexts.component("account", playerName, currencyName, coins));

		// Every special currency is listed (with 0 when the player has none), but only
		// when the server actually has special currencies.
		for (SpecialCurrencyManager.Currency currency : SpecialCurrencyManager.get().currencies()) {
			long amount = SpecialCurrencyManager.get().balance(currency.name, player.getUUID());
			message.append(TreasuryTexts.component("account_currency", currency.name, amount));
		}

		source.sendSuccess(() -> message, false);
		return 1;
	}

	private static int add(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		long amount = LongArgumentType.getLong(context, "amount");
		UUID target = resolve(source, name);

		if (target == null) {
			return 0;
		}

		TreasuryManager.get().add(target, name, amount);
		source.sendSuccess(() -> TreasuryTexts.component("added",
				name, amount, OxycarbideConfig.get().getCurrencyName()), true);
		return 1;
	}

	private static int cut(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		long amount = LongArgumentType.getLong(context, "amount");
		UUID target = resolve(source, name);

		if (target == null) {
			return 0;
		}

		TreasuryManager manager = TreasuryManager.get();

		// Operators may force-deduct: the balance is allowed to go negative.
		long newBalance = manager.forceCut(target, name, amount);

		if (newBalance < 0) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.treasury.cut_negative",
					name, OxycarbideConfig.get().getCurrencyName(), newBalance), true);
		} else {
			source.sendSuccess(() -> TreasuryTexts.component("cut",
					name, amount, OxycarbideConfig.get().getCurrencyName()), true);
		}

		return 1;
	}

	private static int transfer(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		OxycarbideConfig config = OxycarbideConfig.get();

		if (!config.isAllowPlayerTransTreasury()) {
			source.sendFailure(TreasuryTexts.component("transfer_disabled"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		long amount = LongArgumentType.getLong(context, "amount");

		if (name.equalsIgnoreCase(player.getScoreboardName())) {
			source.sendFailure(TreasuryTexts.component("self"));
			return 0;
		}

		long limit = config.getTreasuryTransferLimit();

		if (limit > 0 && amount > limit) {
			source.sendFailure(TreasuryTexts.component("limit", limit, config.getCurrencyName()));
			return 0;
		}

		UUID target = resolve(source, name);

		if (target == null) {
			return 0;
		}

		TreasuryManager manager = TreasuryManager.get();
		long balance = manager.balance(player.getUUID());

		if (balance < amount) {
			source.sendFailure(TreasuryTexts.component("insufficient", player.getScoreboardName(), balance));
			return 0;
		}

		manager.transfer(player.getUUID(), target, name, amount);
		source.sendSuccess(() -> TreasuryTexts.component("transferred",
				name, amount, config.getCurrencyName()), false);

		ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(target);

		if (targetPlayer != null) {
			targetPlayer.sendSystemMessage(TreasuryTexts.component("received",
					player.getScoreboardName(), amount, config.getCurrencyName()));
		}

		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		List<SpecialCurrencyManager.Currency> currencies = SpecialCurrencyManager.get().currencies();
		List<TreasuryManager.Entry> accounts = TreasuryManager.get().all();

		// Every registered account is listed, even with a zero balance: the account is
		// created when the player joins.
		if (accounts.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.treasury.list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.treasury.list_header", accounts.size()), false);

		for (TreasuryManager.Entry account : accounts) {
			MutableComponent line = Component.translatable("oxycarbide.treasury.list_entry",
					account.name, OxycarbideConfig.get().getCurrencyName(), account.balance);
			UUID owner = uuidOf(account.uuid);

			if (owner != null) {
				for (SpecialCurrencyManager.Currency currency : currencies) {
					long amount = SpecialCurrencyManager.get().balance(currency.name, owner);
					line.append(Component.translatable("oxycarbide.treasury.list_currency", currency.name, amount));
				}
			}

			source.sendSuccess(() -> line, false);
		}

		return accounts.size();
	}

	private static UUID uuidOf(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	/** Resolves an online player first, then a stored offline account. */
	private static UUID resolve(CommandSourceStack source, String name) {
		ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(name);

		if (online != null) {
			return online.getUUID();
		}

		UUID stored = TreasuryManager.get().findByName(name);

		if (stored == null) {
			source.sendFailure(TreasuryTexts.component("player_not_found", name));
			return null;
		}

		return stored;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isTreasuryEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.treasury.disabled"));
		return false;
	}
}
