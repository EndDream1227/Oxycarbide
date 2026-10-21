package com.kaze943.oxycarbide.mute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores mutes in {@code <world>/oxycarbide_mutes.json}.
 *
 * <p>Expiry is stored as an absolute wall-clock timestamp, so mutes survive server
 * restarts; {@code expiresAt == 0} means a permanent mute that only {@code /unmute}
 * can lift.</p>
 */
public final class MuteManager {
	private static final MuteManager INSTANCE = new MuteManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, MuteEntry>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_mutes.json";

	private final Map<String, MuteEntry> mutes = new LinkedHashMap<>();
	private Path file;

	private MuteManager() {
	}

	public static MuteManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		mutes.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, MuteEntry> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				mutes.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read mutes from {}", file, e);
		}
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(mutes, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write mutes to {}", file, e);
		}
	}

	public boolean isMuted(UUID player) {
		MuteEntry entry = mutes.get(player.toString());

		if (entry == null) {
			return false;
		}

		if (entry.isExpired()) {
			mutes.remove(player.toString());
			return false;
		}

		return true;
	}

	/**
	 * @return remaining seconds, or {@code -1} for a permanent mute
	 */
	public long remainingSeconds(UUID player) {
		MuteEntry entry = mutes.get(player.toString());

		if (entry == null || entry.isExpired()) {
			return 0;
		}

		if (entry.isPermanent()) {
			return -1;
		}

		long millis = Math.max(0, entry.expiresAt - System.currentTimeMillis());
		return (millis + 999) / 1000;
	}

	public void mute(UUID player, String name, int ticks) {
		mutes.put(player.toString(), new MuteEntry(name, System.currentTimeMillis() + ticks * 50L));
	}

	public void mutePermanently(UUID player, String name) {
		mutes.put(player.toString(), new MuteEntry(name, 0L));
	}

	/** @return {@code true} if the player was muted and is now unmuted */
	public boolean unmute(UUID player) {
		return mutes.remove(player.toString()) != null;
	}

	public void clear() {
		mutes.clear();
	}

	static final class MuteEntry {
		String name;
		/** Epoch millis, or {@code 0} for a permanent mute. */
		long expiresAt;

		MuteEntry() {
		}

		MuteEntry(String name, long expiresAt) {
			this.name = name;
			this.expiresAt = expiresAt;
		}

		boolean isPermanent() {
			return expiresAt == 0L;
		}

		boolean isExpired() {
			return !isPermanent() && System.currentTimeMillis() >= expiresAt;
		}
	}
}
