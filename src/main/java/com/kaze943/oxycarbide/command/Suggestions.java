package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.home.HomeManager;
import com.kaze943.oxycarbide.market.MarketListing;
import com.kaze943.oxycarbide.market.MarketManager;
import com.kaze943.oxycarbide.shop.ShopDefinition;
import com.kaze943.oxycarbide.shop.ShopManager;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.warp.Warp;
import com.kaze943.oxycarbide.warp.WarpManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Tab completion for the parts of a command that are not fixed words: player names,
 * home names, warp names, shop ids, currencies and the like.
 *
 * <p>Brigadier asks these providers while the player is typing, so pressing tab (or
 * opening the suggestion list) fills the value in without spelling it out.</p>
 */
public final class Suggestions {
	public static final SuggestionProvider<CommandSourceStack> PLAYERS =
			(context, builder) -> suggest(builder, playerNames(context));

	public static final SuggestionProvider<CommandSourceStack> BOOLEANS =
			(context, builder) -> suggest(builder, List.of("true", "false"));

	public static final SuggestionProvider<CommandSourceStack> HOMES =
			(context, builder) -> suggest(builder, ownHomeNames(context));

	/** Every home name of every player, for {@code /opdelhome <home> <player>}. */
	public static final SuggestionProvider<CommandSourceStack> ALL_HOMES =
			(context, builder) -> suggest(builder, HomeManager.get().allNames());

	public static final SuggestionProvider<CommandSourceStack> WARPS =
			(context, builder) -> suggest(builder, warpNames());

	public static final SuggestionProvider<CommandSourceStack> SHOPS =
			(context, builder) -> suggest(builder, shopIds());

	public static final SuggestionProvider<CommandSourceStack> MARKET_IDS =
			(context, builder) -> suggest(builder, marketIds());

	public static final SuggestionProvider<CommandSourceStack> CURRENCIES =
			(context, builder) -> suggest(builder, currencies());

	public static final SuggestionProvider<CommandSourceStack> SWEEP_WHITELIST =
			(context, builder) -> suggest(builder, OxycarbideConfig.get().getSweepWhitelist());

	private Suggestions() {
	}

	private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggest(
			SuggestionsBuilder builder, Collection<String> values) {
		String typed = builder.getRemaining().toLowerCase(Locale.ROOT);

		for (String value : values) {
			if (value == null || value.isBlank()) {
				continue;
			}

			if (typed.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(typed)) {
				builder.suggest(value);
			}
		}

		return builder.buildFuture();
	}

	private static List<String> playerNames(CommandContext<CommandSourceStack> context) {
		List<String> names = new ArrayList<>();

		if (context.getSource().getServer() == null) {
			return names;
		}

		for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
			names.add(player.getScoreboardName());
		}

		return names;
	}

	private static List<String> ownHomeNames(CommandContext<CommandSourceStack> context) {
		ServerPlayer player = context.getSource().getPlayer();
		return player == null ? List.of() : HomeManager.get().names(player.getUUID());
	}

	private static List<String> warpNames() {
		List<String> names = new ArrayList<>();

		for (Warp warp : WarpManager.get().all()) {
			names.add(warp.name());
		}

		return names;
	}

	private static List<String> shopIds() {
		List<String> ids = new ArrayList<>();

		for (ShopDefinition shop : ShopManager.get().all()) {
			ids.add(shop.id);
		}

		return ids;
	}

	private static List<String> marketIds() {
		List<String> ids = new ArrayList<>();

		for (MarketListing listing : MarketManager.get().all()) {
			ids.add(listing.id);
		}

		return ids;
	}

	/** {@code normal} (coins) plus every special currency, as {@code /sell} and claims take them. */
	private static List<String> currencies() {
		List<String> names = new ArrayList<>(List.of("normal"));

		for (SpecialCurrencyManager.Currency currency : SpecialCurrencyManager.get().currencies()) {
			if (currency.name != null && !currency.name.isBlank() && !names.contains(currency.name)) {
				names.add(currency.name);
			}
		}

		return names;
	}
}
