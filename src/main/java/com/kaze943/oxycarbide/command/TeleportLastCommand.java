package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.tracking.LastPlayerTracker;
import com.kaze943.oxycarbide.util.Admins;
import com.kaze943.oxycarbide.util.Teleporter;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /telepoint_last}: teleports the operator to the place where the most recently
 * seen player was.
 */
public final class TeleportLastCommand {
	private TeleportLastCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("telepoint_last")
						.requires(Admins::isAdmin)
						.executes(TeleportLastCommand::teleport)));
	}

	private static int teleport(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		LastPlayerTracker.Spot spot = LastPlayerTracker.get().getSpot();

		if (spot == null) {
			source.sendFailure(Component.translatable("oxycarbide.teleportlast.none"));
			return 0;
		}

		ServerLevel level = Teleporter.findLevel(source.getServer(), spot.dimension);

		if (level == null) {
			source.sendFailure(Component.translatable("oxycarbide.teleportlast.dimension_missing", spot.dimension));
			return 0;
		}

		Teleporter.teleport(player, level, spot.x, spot.y, spot.z, spot.yaw, spot.pitch);
		source.sendSuccess(() -> Component.translatable("oxycarbide.teleportlast.done", spot.name), false);
		return 1;
	}
}
