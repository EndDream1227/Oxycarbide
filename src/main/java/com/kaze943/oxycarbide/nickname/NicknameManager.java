package com.kaze943.oxycarbide.nickname;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores chat nicknames per player in {@code <world>/oxycarbide_nicknames.json}.
 *
 * <p>The mixin on {@code Player#getDisplayName()} asks this class for the nickname, so
 * vanilla chat, the tab list and name tags all use it without every call site being
 * patched.</p>
 */
public final class NicknameManager {
	public static final int MAX_LENGTH = 32;

	private static final NicknameManager INSTANCE = new NicknameManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, String>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_nicknames.json";

	private final Map<String, String> nicknames = new LinkedHashMap<>();
	private final Map<String, Component> components = new HashMap<>();
	private Path file;

	private NicknameManager() {
	}

	public static NicknameManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		nicknames.clear();
		components.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, String> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				nicknames.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read nicknames from {}", file, e);
		}
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(nicknames, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write nicknames to {}", file, e);
		}
	}

	public void set(UUID player, String nickname) {
		String key = player.toString();
		nicknames.put(key, nickname);
		components.put(key, Component.literal(nickname));
	}

	/** @return the nickname, or {@code null} if the player has none */
	public String get(UUID player) {
		return nicknames.get(player.toString());
	}

	/**
	 * Used by the display name mixin.
	 *
	 * @return a literal component with the nickname, or {@code null} to keep the vanilla
	 *         name
	 */
	public Component displayName(Player player) {
		return components.get(player.getUUID().toString());
	}

	public void clear() {
		nicknames.clear();
		components.clear();
	}
}
