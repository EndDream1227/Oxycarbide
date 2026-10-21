package com.kaze943.oxycarbide.util;

import com.kaze943.oxycarbide.travel.BackPoints;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * Cross-dimension teleporting and level lookup, isolated here so a mapping change only
 * has to be fixed in one place.
 */
public final class Teleporter {
	private Teleporter() {
	}

	public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
		// Remember where the player came from so /back can return there. Doing it here
		// covers every teleport of the mod, and makes /back toggle between two points.
		BackPoints.remember(player, (ServerLevel) player.level(),
				player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

		// Absolute rotation, no relative movement flags. The trailing boolean is
		// "keep the camera entity in sync".
		player.teleportTo(level, x, y, z, Set.<Relative>of(), yaw, pitch, false);

		// Do not let the old fall damage or momentum follow the player.
		player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
	}

	/**
	 * Resolves a stored dimension id such as {@code minecraft:overworld} to the level of
	 * the running server.
	 *
	 * @return {@code null} if no level with that dimension id is loaded
	 */
	public static ServerLevel findLevel(MinecraftServer server, String dimension) {
		for (ServerLevel level : server.getAllLevels()) {
			if (level.dimension().identifier().toString().equals(dimension)) {
				return level;
			}
		}

		return null;
	}
}
