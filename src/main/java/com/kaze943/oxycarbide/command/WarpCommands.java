package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.util.Admins;
import com.kaze943.oxycarbide.util.Teleporter;
import com.kaze943.oxycarbide.util.TextComponents;
import com.kaze943.oxycarbide.warp.Warp;
import com.kaze943.oxycarbide.warp.WarpManager;
import com.kaze943.oxycarbide.warp.WarpTexts;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * {@code /warp}, {@code /setwarp}, {@code /delwarp} and {@code /renamewarp}.
 *
 * <p>Warps are shared by the whole server, but each warp remembers who created it:
 * when the matching {@code OnlyOp...Warp} rule is off, players may only edit or delete
 * their own warps.</p>
 */
public final class WarpCommands {
	private static final int MAX_NAME_LENGTH = 32;

	private WarpCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("warp")
					.then(Commands.argument("name", StringArgumentType.word())
							.suggests(Suggestions.WARPS)
							.executes(context -> warp(context, StringArgumentType.getString(context, "name")))));

			dispatcher.register(Commands.literal("setwarp")
					.then(Commands.argument("name", StringArgumentType.word())
							.executes(context -> setWarp(context, StringArgumentType.getString(context, "name")))));

			dispatcher.register(Commands.literal("delwarp")
					.then(Commands.argument("name", StringArgumentType.word())
							.suggests(Suggestions.WARPS)
							.executes(context -> deleteWarp(context, StringArgumentType.getString(context, "name")))));

			dispatcher.register(Commands.literal("renamewarp")
					.then(Commands.argument("old", StringArgumentType.word())
							.suggests(Suggestions.WARPS)
							.then(Commands.argument("new", StringArgumentType.word())
									.executes(context -> renameWarp(context,
											StringArgumentType.getString(context, "old"),
											StringArgumentType.getString(context, "new"))))));

			dispatcher.register(Commands.literal("listwarps")
					.executes(WarpCommands::listWarps));
		});
	}

	private static int listWarps(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		List<Warp> warps = WarpManager.get().all();

		if (warps.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.warp.list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.warp.list_header", warps.size()), false);

		for (Warp warp : warps) {
			String owner = warp.ownerName() == null ? "?" : warp.ownerName();
			source.sendSuccess(() -> Component.translatable("oxycarbide.warp.list_entry",
					owner,
					warp.name(),
					warp.dimension(),
					TextComponents.formatCoordinate(warp.x()),
					TextComponents.formatCoordinate(warp.y()),
					TextComponents.formatCoordinate(warp.z())), false);
		}

		return warps.size();
	}

	private static int warp(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.warp.player_only"));
			return 0;
		}

		Warp warp = WarpManager.get().get(name);

		if (warp == null) {
			source.sendFailure(WarpTexts.component("not_found", name));
			return 0;
		}

		ServerLevel level = Teleporter.findLevel(source.getServer(), warp.dimension());

		if (level == null) {
			source.sendFailure(WarpTexts.component("dimension_missing", warp.name(), warp.dimension()));
			return 0;
		}

		Teleporter.teleport(player, level, warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
		source.sendSuccess(() -> WarpTexts.component("teleport", warp.name()), false);
		return 1;
	}

	private static int setWarp(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.warp.player_only"));
			return 0;
		}

		if (name.length() > MAX_NAME_LENGTH) {
			source.sendFailure(WarpTexts.component("name_too_long", MAX_NAME_LENGTH));
			return 0;
		}

		OxycarbideConfig config = OxycarbideConfig.get();
		boolean admin = Admins.isAdmin(source);

		if (config.isOnlyOpCreateWarp() && !admin) {
			source.sendFailure(WarpTexts.component("create_no_permission"));
			return 0;
		}

		WarpManager manager = WarpManager.get();

		if (manager.get(name) != null) {
			source.sendFailure(WarpTexts.component("exists", name));
			return 0;
		}

		int limit = config.getWarpLimit();

		// Operators are never limited, and 0 means "unlimited" for everybody else.
		if (!admin && limit > 0 && manager.countOwnedBy(player.getUUID()) >= limit) {
			source.sendFailure(WarpTexts.component("limit", limit));
			return 0;
		}

		ServerLevel level = source.getLevel();
		Warp warp = new Warp(name, level.dimension().identifier().toString(),
				player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(),
				player.getUUID().toString(), player.getScoreboardName());

		manager.put(warp);
		manager.save();

		Component coordinates = TextComponents.clickableCoordinates(warp.x(), warp.y(), warp.z());
		source.sendSuccess(() -> WarpTexts.component("set", name, coordinates), false);
		return 1;
	}

	private static int deleteWarp(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.warp.player_only"));
			return 0;
		}

		WarpManager manager = WarpManager.get();
		Warp warp = manager.get(name);

		if (warp == null) {
			source.sendFailure(WarpTexts.component("not_found", name));
			return 0;
		}

		boolean admin = Admins.isAdmin(source);

		if (OxycarbideConfig.get().isOnlyOpDeleteWarp() && !admin) {
			source.sendFailure(WarpTexts.component("delete_no_permission"));
			return 0;
		}

		if (!admin && !warp.isOwnedBy(player.getUUID())) {
			source.sendFailure(WarpTexts.component("not_owner", warp.name()));
			return 0;
		}

		manager.remove(name);
		manager.save();
		source.sendSuccess(() -> WarpTexts.component("deleted", warp.name()), true);
		return 1;
	}

	private static int renameWarp(CommandContext<CommandSourceStack> context, String oldName, String newName) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.warp.player_only"));
			return 0;
		}

		if (newName.length() > MAX_NAME_LENGTH) {
			source.sendFailure(WarpTexts.component("name_too_long", MAX_NAME_LENGTH));
			return 0;
		}

		WarpManager manager = WarpManager.get();
		Warp warp = manager.get(oldName);

		if (warp == null) {
			source.sendFailure(WarpTexts.component("not_found", oldName));
			return 0;
		}

		boolean admin = Admins.isAdmin(source);

		if (OxycarbideConfig.get().isOnlyOpEditWarp() && !admin) {
			source.sendFailure(WarpTexts.component("edit_no_permission"));
			return 0;
		}

		if (!admin && !warp.isOwnedBy(player.getUUID())) {
			source.sendFailure(WarpTexts.component("not_owner", warp.name()));
			return 0;
		}

		if (manager.get(newName) != null) {
			source.sendFailure(WarpTexts.component("rename_exists", newName));
			return 0;
		}

		String oldDisplayName = warp.name();
		manager.rename(oldName, newName);
		manager.save();
		source.sendSuccess(() -> WarpTexts.component("renamed", oldDisplayName, newName), true);
		return 1;
	}

	/** Every warp command refuses to run while {@code /oxycarbide config warp switch false} is active. */
	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isWarpEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.warp.disabled"));
		return false;
	}
}
