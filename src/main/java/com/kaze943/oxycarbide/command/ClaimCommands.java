package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.claim.Claim;
import com.kaze943.oxycarbide.claim.ClaimManager;
import com.kaze943.oxycarbide.claim.ClaimTexts;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.treasury.TreasuryManager;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

/**
 * {@code /claim}, {@code /chunkbelong}, {@code /claim_transfer}, {@code /disclaim},
 * {@code /sp_disclaim} and {@code /claim_settings}.
 */
public final class ClaimCommands {
	private ClaimCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("claim")
					.executes(ClaimCommands::claim));

			dispatcher.register(Commands.literal("chunkbelong")
					.executes(ClaimCommands::belong));

			dispatcher.register(Commands.literal("claim_transfer")
					.then(Commands.argument("player", StringArgumentType.word())
							.suggests(Suggestions.PLAYERS)
							.executes(ClaimCommands::transfer)));

			dispatcher.register(Commands.literal("disclaim")
					.executes(ClaimCommands::disclaim));

			dispatcher.register(Commands.literal("forceload")
					.executes(ClaimCommands::forceLoad));

			dispatcher.register(Commands.literal("forceload_cancle")
					.executes(ClaimCommands::forceLoadCancel));

			dispatcher.register(Commands.literal("forceload_spcancle")
					.requires(Admins::isAdmin)
					.executes(ClaimCommands::forceLoadAdminCancel));

			dispatcher.register(Commands.literal("sp_disclaim")
					.requires(Admins::isAdmin)
					.executes(ClaimCommands::forceDisclaim));

			dispatcher.register(Commands.literal("claim_settings")
					.then(Commands.literal("whitelist")
							.then(Commands.literal("add")
									.then(Commands.argument("player", StringArgumentType.word())
											.suggests(Suggestions.PLAYERS)
											.executes(ClaimCommands::whitelistAdd)))
							.then(Commands.literal("remove")
									.then(Commands.argument("player", StringArgumentType.word())
											.suggests(Suggestions.PLAYERS)
											.executes(ClaimCommands::whitelistRemove))))
					.then(Commands.literal("grounded")
							.then(Commands.argument("value", BoolArgumentType.bool())
									.suggests(Suggestions.BOOLEANS)
									.executes(ClaimCommands::setGrounded)))
					.then(Commands.literal("openToTeam")
							.then(Commands.argument("value", BoolArgumentType.bool())
									.suggests(Suggestions.BOOLEANS)
									.executes(ClaimCommands::setOpenToTeam)))
					.then(Commands.literal("publicopen")
							.then(Commands.argument("value", BoolArgumentType.bool())
									.suggests(Suggestions.BOOLEANS)
									.executes(ClaimCommands::setPublicOpen))));
		});
	}

	private static int claim(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		OxycarbideConfig config = OxycarbideConfig.get();
		boolean admin = Admins.isAdmin(source);

		if (config.isOnlyOpPlayerClaim() && !admin) {
			source.sendFailure(ClaimTexts.component("only_op"));
			return 0;
		}

		ServerLevel level = (ServerLevel) player.level();
		BlockPos pos = player.blockPosition();
		ClaimManager manager = ClaimManager.get();
		Claim existing = manager.claimAt(level, pos);

		if (existing != null) {
			source.sendFailure(ClaimTexts.component("already_claimed", existing.ownerName));
			return 0;
		}

		int max = config.getClaimMaxNum();

		if (!admin && max > 0 && manager.countOwnedBy(player.getUUID()) >= max) {
			source.sendFailure(ClaimTexts.component("limit", max));
			return 0;
		}

		long cost = config.getClaimCostAmount();

		if (cost > 0 && !pay(source, player, cost, config.getClaimCostCurrency())) {
			return 0;
		}

		manager.claim(level, pos, player);
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		source.sendSuccess(() -> ClaimTexts.component("claimed", chunkX, chunkZ), true);
		return 1;
	}

	private static int belong(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (claim == null) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.claim.unclaimed"), false);
			return 1;
		}

		source.sendSuccess(() -> ClaimTexts.component("belong", claim.ownerName), false);
		source.sendSuccess(() -> ClaimTexts.component("belong_settings",
				stateOf(claim.grounded), stateOf(claim.openToTeam), stateOf(claim.publicOpen),
				stateOf(claim.forceLoaded),
				claim.whitelist == null ? 0 : claim.whitelist.size()), false);
		return 1;
	}

	private static int transfer(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (claim == null) {
			source.sendFailure(ClaimTexts.component("no_claim"));
			return 0;
		}

		if (!claim.owner.equals(player.getUUID().toString()) && !Admins.isAdmin(source)) {
			source.sendFailure(ClaimTexts.component("not_owner", claim.ownerName));
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(name);

		if (target == null) {
			source.sendFailure(Component.translatable("oxycarbide.claim.player_not_found", name));
			return 0;
		}

		if (target.getUUID().toString().equals(claim.owner)) {
			source.sendFailure(Component.translatable("oxycarbide.claim.already_owner", target.getScoreboardName()));
			return 0;
		}

		claim.owner = target.getUUID().toString();
		claim.ownerName = target.getScoreboardName();
		ClaimManager.get().save();
		source.sendSuccess(() -> ClaimTexts.component("transferred", claim.ownerName), true);
		return 1;
	}

	private static int disclaim(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (claim == null) {
			source.sendFailure(ClaimTexts.component("no_claim"));
			return 0;
		}

		if (!claim.owner.equals(player.getUUID().toString())) {
			source.sendFailure(ClaimTexts.component("not_owner", claim.ownerName));
			return 0;
		}

		removeAtFeet(player);
		source.sendSuccess(() -> ClaimTexts.component("disclaimed"), true);
		return 1;
	}

	private static int forceDisclaim(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (claimAtFeet(player) == null) {
			source.sendFailure(ClaimTexts.component("no_claim"));
			return 0;
		}

		removeAtFeet(player);
		source.sendSuccess(() -> Component.translatable("oxycarbide.claim.sp_disclaimed"), true);
		return 1;
	}

	private static int whitelistAdd(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(name);

		if (target == null) {
			source.sendFailure(Component.translatable("oxycarbide.claim.player_not_found", name));
			return 0;
		}

		if (claim.whitelist == null) {
			claim.whitelist = new java.util.LinkedHashMap<>();
		}

		claim.whitelist.put(target.getUUID().toString(), target.getScoreboardName());
		ClaimManager.get().save();
		source.sendSuccess(() -> ClaimTexts.component("whitelist_added", target.getScoreboardName()), true);
		return 1;
	}

	private static int whitelistRemove(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		String removedName = removeWhitelisted(claim, name);

		if (removedName == null) {
			source.sendFailure(ClaimTexts.component("not_whitelisted", name));
			return 0;
		}

		ClaimManager.get().save();
		source.sendSuccess(() -> ClaimTexts.component("whitelist_removed", removedName), true);
		return 1;
	}

	private static int setGrounded(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		boolean value = BoolArgumentType.getBool(context, "value");
		claim.grounded = value;
		ClaimManager.get().save();
		source.sendSuccess(() -> ClaimTexts.component(value
				? "grounded_on"
				: "grounded_off"), true);
		return 1;
	}

	private static int setOpenToTeam(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		boolean value = BoolArgumentType.getBool(context, "value");
		claim.openToTeam = value;
		ClaimManager.get().save();
		source.sendSuccess(() -> ClaimTexts.component(value
				? "team_on"
				: "team_off"), true);
		return 1;
	}

	private static int setPublicOpen(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		boolean value = BoolArgumentType.getBool(context, "value");
		claim.publicOpen = value;
		ClaimManager.get().save();
		source.sendSuccess(() -> ClaimTexts.component(value
				? "publicopen_on"
				: "publicopen_off"), true);
		return 1;
	}

	private static int forceLoad(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		if (claim.forceLoaded) {
			source.sendFailure(ClaimTexts.component("forceload_already"));
			return 0;
		}

		OxycarbideConfig config = OxycarbideConfig.get();
		int max = config.getClaimForceloadMaxNum();

		if (max <= 0) {
			source.sendFailure(ClaimTexts.component("forceload_forbidden"));
			return 0;
		}

		int claimMax = config.getClaimMaxNum();

		if (claimMax > 0 && max > claimMax) {
			max = claimMax;
		}

		ClaimManager manager = ClaimManager.get();

		if (manager.countForceLoaded(player.getUUID()) >= max) {
			int limit = max;
			source.sendFailure(ClaimTexts.component("forceload_limit", limit));
			return 0;
		}

		ServerLevel level = (ServerLevel) player.level();
		manager.setForceLoaded(claim, level, player.blockPosition(), true);
		source.sendSuccess(() -> ClaimTexts.component("forceload_loaded"), true);
		return 1;
	}

	private static int forceLoadCancel(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (!ensureOwnership(source, claim, player)) {
			return 0;
		}

		if (!claim.forceLoaded) {
			source.sendFailure(ClaimTexts.component("forceload_not_loaded"));
			return 0;
		}

		ServerLevel level = (ServerLevel) player.level();
		ClaimManager.get().setForceLoaded(claim, level, player.blockPosition(), false);
		source.sendSuccess(() -> ClaimTexts.component("forceload_cancelled"), true);
		return 1;
	}

	private static int forceLoadAdminCancel(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		Claim claim = claimAtFeet(player);

		if (claim == null) {
			source.sendFailure(ClaimTexts.component("no_claim"));
			return 0;
		}

		if (!claim.forceLoaded) {
			source.sendFailure(ClaimTexts.component("forceload_not_loaded"));
			return 0;
		}

		ServerLevel level = (ServerLevel) player.level();
		ClaimManager.get().setForceLoaded(claim, level, player.blockPosition(), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.claim.forceload.sp_cancelled"), true);
		return 1;
	}

	/** @return {@code true} if the player owns the claim (or is an operator) */
	private static boolean ensureOwnership(CommandSourceStack source, Claim claim, ServerPlayer player) {
		if (claim == null) {
			source.sendFailure(ClaimTexts.component("no_claim"));
			return false;
		}

		if (!claim.owner.equals(player.getUUID().toString()) && !Admins.isAdmin(source)) {
			source.sendFailure(ClaimTexts.component("not_owner", claim.ownerName));
			return false;
		}

		return true;
	}

	private static Claim claimAtFeet(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		return ClaimManager.get().claimAt(level, player.blockPosition());
	}

	private static void removeAtFeet(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		ClaimManager.get().remove(level, player.blockPosition());
	}

	private static String removeWhitelisted(Claim claim, String name) {
		if (claim.whitelist == null || claim.whitelist.isEmpty()) {
			return null;
		}

		for (Map.Entry<String, String> entry : claim.whitelist.entrySet()) {
			if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(name)) {
				claim.whitelist.remove(entry.getKey());
				return entry.getValue();
			}
		}

		return null;
	}

	private static boolean pay(CommandSourceStack source, ServerPlayer player, long amount, String currency) {
		OxycarbideConfig config = OxycarbideConfig.get();

		if ("normal".equalsIgnoreCase(currency)) {
			long balance = TreasuryManager.get().balance(player.getUUID());

			if (balance < amount) {
				source.sendFailure(ClaimTexts.component("insufficient",
						amount, config.getCurrencyName(), balance));
				return false;
			}

			TreasuryManager.get().forceCut(player.getUUID(), player.getScoreboardName(), amount);
			return true;
		}

		SpecialCurrencyManager manager = SpecialCurrencyManager.get();

		if (!manager.exists(currency)) {
			source.sendFailure(Component.translatable("oxycarbide.claim.currency_missing", currency));
			return false;
		}

		long balance = manager.balance(currency, player.getUUID());

		if (balance < amount) {
			source.sendFailure(ClaimTexts.component("insufficient",
					amount, currency, balance));
			return false;
		}

		manager.forceCut(currency, player.getUUID(), player.getScoreboardName(), amount);
		return true;
	}

	private static Component stateOf(boolean value) {
		return Component.translatable(value ? "oxycarbide.value.on" : "oxycarbide.value.off");
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isClaimEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.claim.disabled"));
		return false;
	}
}
