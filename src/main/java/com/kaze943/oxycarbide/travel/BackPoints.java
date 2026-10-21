package com.kaze943.oxycarbide.travel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Remembers the last place a player wants to go back to: the spot they were teleported
 * away from, or the spot where they died.
 *
 * <p>The map lives in memory only, so it is reset when the server stops. Vanilla already
 * persists the last death location on the player itself, but this keeps both cases in a
 * single place and lets {@code /back} toggle between two points.</p>
 */
public final class BackPoints {
	private static final Map<UUID, Location> POINTS = new HashMap<>();

	private BackPoints() {
	}

	public static void remember(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
		POINTS.put(player.getUUID(), new Location(level.dimension().identifier().toString(), x, y, z, yaw, pitch));
	}

	/** @return {@code null} if the player has no remembered position */
	public static Location get(UUID player) {
		return POINTS.get(player);
	}

	public static void clear() {
		POINTS.clear();
	}

	public record Location(String dimension, double x, double y, double z, float yaw, float pitch) {
	}
}
