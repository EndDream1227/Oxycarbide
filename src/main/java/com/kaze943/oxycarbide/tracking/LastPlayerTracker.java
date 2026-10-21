package com.kaze943.oxycarbide.tracking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kaze943.oxycarbide.Oxycarbide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Remembers where the most recently seen player was, so {@code /telepoint_last} can take
 * an operator there.
 *
 * <p>The spot is refreshed whenever a player joins or disconnects and is persisted in
 * {@code <world>/oxycarbide_lastplayer.json}, so it survives server restarts.</p>
 */
public final class LastPlayerTracker {
	private static final LastPlayerTracker INSTANCE = new LastPlayerTracker();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "oxycarbide_lastplayer.json";

	private Spot spot;
	private Path file;

	private LastPlayerTracker() {
	}

	public static LastPlayerTracker get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		spot = null;
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			spot = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Spot.class);
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read the last player position from {}", file, e);
		}
	}

	public void save() {
		if (file == null || spot == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(spot), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the last player position to {}", file, e);
		}
	}

	public void update(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();

		spot = new Spot();
		spot.name = player.getScoreboardName();
		spot.dimension = level.dimension().identifier().toString();
		spot.x = player.getX();
		spot.y = player.getY();
		spot.z = player.getZ();
		spot.yaw = player.getYRot();
		spot.pitch = player.getXRot();
		save();
	}

	/** @return {@code null} if no player has been seen yet */
	public Spot getSpot() {
		return spot;
	}

	public void clear() {
		spot = null;
	}

	/** Serialized shape of a remembered position. */
	public static final class Spot {
		public String name = "";
		public String dimension = "";
		public double x;
		public double y;
		public double z;
		public float yaw;
		public float pitch;
	}
}
