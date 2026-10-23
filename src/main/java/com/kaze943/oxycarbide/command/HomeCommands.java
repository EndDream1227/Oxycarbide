package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.home.Home;
import com.kaze943.oxycarbide.home.HomeManager;
import com.kaze943.oxycarbide.home.HomeTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.kaze943.oxycarbide.util.Teleporter;
import com.kaze943.oxycarbide.util.TextComponents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * {@code /sethome}, {@code /delhome}, {@code /home}, {@code /listhome} and {@code /opdelhome}.
 */
public final class HomeCommands {
	private static final int MAX_NAME_LENGTH = 32;

	private HomeCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("sethome")
					.then(Commands.argument("name", StringArgumentType.word())
							.executes(context -> setHome(context, StringArgumentType.getString(context, "name")))));

			dispatcher.register(Commands.literal("delhome")
					.then(Commands.argument("name", StringArgumentType.word())
							.suggests(Suggestions.HOMES)
							.executes(context -> deleteHome(context, StringArgumentType.getString(context, "name")))));

			dispatcher.register(Commands.literal("home")
					.then(Commands.argument("name", StringArgumentType.word())
							.suggests(Suggestions.HOMES)
							.executes(context -> teleportHome(context, StringArgumentType.getString(context, "name")))));

			dispatcher.register(Commands.literal("listhome")
					.requires(Admins::isAdmin)
					.executes(HomeCommands::listHomes));

			dispatcher.register(Commands.literal("opdelhome")
					.requires(Admins::isAdmin)
					.then(Commands.argument("home", StringArgumentType.word())
							.suggests(Suggestions.ALL_HOMES)
							.then(Commands.argument("player", StringArgumentType.word())
									.suggests(Suggestions.PLAYERS)
									.executes(context -> deleteHomeOfPlayer(context,
											StringArgumentType.getString(context, "home"),
											StringArgumentType.getString(context, "player"))))));
		});
	}

	private static int setHome(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.home.player_only"));
			return 0;
		}

		if (name.length() > MAX_NAME_LENGTH) {
			source.sendFailure(HomeTexts.component("name_too_long", MAX_NAME_LENGTH));
			return 0;
		}

		HomeManager manager = HomeManager.get();
		UUID uuid = player.getUUID();

		if (manager.get(uuid, name) != null) {
			source.sendFailure(HomeTexts.component("exists", name));
			return 0;
		}

		int limit = OxycarbideConfig.get().getHomeLimit();

		// Operators are never limited, and 0 means "unlimited" for everybody else.
		if (!Admins.isAdmin(source) && limit > 0 && manager.count(uuid) >= limit) {
			source.sendFailure(HomeTexts.component("limit", limit));
			return 0;
		}

		ServerLevel level = (ServerLevel) player.level();
		Home home = new Home(name, level.dimension().identifier().toString(),
				player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

		manager.set(uuid, player.getScoreboardName(), home);
		manager.save();

		Component coordinates = TextComponents.clickableCoordinates(home.x(), home.y(), home.z());
		source.sendSuccess(() -> HomeTexts.component("set", name, coordinates), false);
		return 1;
	}

	private static int deleteHome(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.home.player_only"));
			return 0;
		}

		HomeManager manager = HomeManager.get();

		if (!manager.remove(player.getUUID(), name)) {
			source.sendFailure(HomeTexts.component("not_found", name));
			return 0;
		}

		manager.save();
		source.sendSuccess(() -> HomeTexts.component("deleted", name), false);
		return 1;
	}

	private static int teleportHome(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.home.player_only"));
			return 0;
		}

		Home home = HomeManager.get().get(player.getUUID(), name);

		if (home == null) {
			source.sendFailure(HomeTexts.component("not_found", name));
			return 0;
		}

		ServerLevel level = Teleporter.findLevel(source.getServer(), home.dimension());

		if (level == null) {
			source.sendFailure(HomeTexts.component("dimension_missing", name, home.dimension()));
			return 0;
		}

		Teleporter.teleport(player, level, home.x(), home.y(), home.z(), home.yaw(), home.pitch());
		source.sendSuccess(() -> HomeTexts.component("teleport", name), false);
		return 1;
	}

	private static int listHomes(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		List<HomeManager.Entry> entries = HomeManager.get().all();

		if (entries.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.home.list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.home.list_header", entries.size()), false);

		for (HomeManager.Entry entry : entries) {
			Home home = entry.home();
			source.sendSuccess(() -> Component.translatable("oxycarbide.home.list_entry",
					entry.playerName(),
					home.name(),
					home.dimension(),
					TextComponents.formatCoordinate(home.x()),
					TextComponents.formatCoordinate(home.y()),
					TextComponents.formatCoordinate(home.z())), false);
		}

		return entries.size();
	}

	private static int deleteHomeOfPlayer(CommandContext<CommandSourceStack> context, String homeName, String playerName) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		HomeManager manager = HomeManager.get();
		MinecraftServer server = source.getServer();
		ServerPlayer online = server.getPlayerList().getPlayerByName(playerName);
		UUID uuid = online != null ? online.getUUID() : manager.findByName(playerName);

		if (uuid == null) {
			source.sendFailure(Component.translatable("oxycarbide.home.opdel.player_not_found", playerName));
			return 0;
		}

		if (!manager.remove(uuid, homeName)) {
			source.sendFailure(Component.translatable("oxycarbide.home.opdel.not_found", playerName, homeName));
			return 0;
		}

		manager.save();
		source.sendSuccess(() -> Component.translatable("oxycarbide.home.opdel.deleted", playerName, homeName), true);
		return 1;
	}

	/** Every home command refuses to run while {@code /oxycarbide config home switch false} is active. */
	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isHomeEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.home.disabled"));
		return false;
	}
}
