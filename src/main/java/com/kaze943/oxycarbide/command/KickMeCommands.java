package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /kickme}: disconnects the player who ran it, so somebody who got stuck inside
 * blocks can get out without waiting for an operator.
 *
 * <p>This uses the normal disconnect packet, so the server saves the player data and the
 * client is shown the usual "disconnected" screen instead of being dropped silently.</p>
 */
public final class KickMeCommands {
	private KickMeCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("kickme")
						.executes(KickMeCommands::kickMe)));
	}

	private static int kickMe(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!OxycarbideConfig.get().isKickMeEnabled()) {
			source.sendFailure(Component.translatable("oxycarbide.kickme.disabled"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		player.connection.disconnect(Component.translatable("oxycarbide.kickme.disconnect"));
		return 1;
	}
}
