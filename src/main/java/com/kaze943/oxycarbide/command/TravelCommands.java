package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.travel.BackPoints;
import com.kaze943.oxycarbide.travel.BackTexts;
import com.kaze943.oxycarbide.travel.RtpManager;
import com.kaze943.oxycarbide.travel.SpawnTexts;
import com.kaze943.oxycarbide.util.Teleporter;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData;

/**
 * {@code /spawn}, {@code /back} and {@code /rtp}.
 */
public final class TravelCommands {
	private TravelCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("spawn").executes(TravelCommands::spawn));
			dispatcher.register(Commands.literal("back").executes(TravelCommands::back));
			dispatcher.register(Commands.literal("rtp").executes(TravelCommands::randomTeleport));
		});
	}

	private static int spawn(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!OxycarbideConfig.get().isSpawnEnabled()) {
			source.sendFailure(Component.translatable("oxycarbide.spawn.disabled"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		ServerLevel overworld = source.getServer().overworld();
		LevelData.RespawnData respawn = overworld.getRespawnData();
		ServerLevel level = source.getServer().getLevel(respawn.dimension());

		if (level == null) {
			level = overworld;
		}

		BlockPos pos = respawn.pos();

		Teleporter.teleport(player, level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				respawn.yaw(), respawn.pitch());
		source.sendSuccess(() -> SpawnTexts.component("teleport"), false);
		return 1;
	}

	private static int back(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!OxycarbideConfig.get().isBackEnabled()) {
			source.sendFailure(Component.translatable("oxycarbide.back.disabled"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		BackPoints.Location location = BackPoints.get(player.getUUID());

		if (location == null) {
			source.sendFailure(BackTexts.component("none"));
			return 0;
		}

		ServerLevel level = Teleporter.findLevel(source.getServer(), location.dimension());

		if (level == null) {
			source.sendFailure(BackTexts.component("dimension_missing", location.dimension()));
			return 0;
		}

		Teleporter.teleport(player, level, location.x(), location.y(), location.z(), location.yaw(), location.pitch());
		source.sendSuccess(() -> BackTexts.component("teleport"), false);
		return 1;
	}

	private static int randomTeleport(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!OxycarbideConfig.get().isRtpEnabled()) {
			source.sendFailure(Component.translatable("oxycarbide.rtp.disabled"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		return RtpManager.get().request(source, player) ? 1 : 0;
	}
}
