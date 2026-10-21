package com.kaze943.oxycarbide.home;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Stores every player's homes and keeps them in {@code <world>/oxycarbide_homes.json}.
 *
 * <p>Keys of the inner map are the lower-cased home names, so {@code /home Base} and
 * {@code /home base} refer to the same home.</p>
 */
public final class HomeManager {
	private static final HomeManager INSTANCE = new HomeManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, PlayerHomes>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_homes.json";

	private final Map<String, PlayerHomes> players = new LinkedHashMap<>();
	private Path file;

	private HomeManager() {
	}

	public static HomeManager get() {
		return INSTANCE;
	}

	/** (Re)loads the homes of the world that is currently running. */
	public void load(MinecraftServer server) {
		players.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, PlayerHomes> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				players.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read homes from {}", file, e);
		}
	}

	/** Writes the current state to disk. Safe to call often, the file is small. */
	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(players, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write homes to {}", file, e);
		}
	}

	public Home get(UUID player, String name) {
		PlayerHomes homes = players.get(player.toString());

		if (homes == null) {
			return null;
		}

		return homes.homes.get(normalize(name));
	}

	public int count(UUID player) {
		PlayerHomes homes = players.get(player.toString());
		return homes == null ? 0 : homes.homes.size();
	}

	/**
	 * Adds a home for the given player.
	 *
	 * @return {@code false} if the player already has a home with that name
	 */
	public boolean set(UUID player, String playerName, Home home) {
		PlayerHomes homes = players.computeIfAbsent(player.toString(), key -> new PlayerHomes());
		homes.name = playerName;

		String key = normalize(home.name);

		if (homes.homes.containsKey(key)) {
			return false;
		}

		homes.homes.put(key, home);
		return true;
	}

	/** @return {@code true} if a home with that name existed and was removed */
	public boolean remove(UUID player, String name) {
		PlayerHomes homes = players.get(player.toString());
		return homes != null && homes.homes.remove(normalize(name)) != null;
	}

	/** Every home of every player, sorted by player name and then home name. */
	public List<Entry> all() {
		List<Entry> result = new ArrayList<>();

		for (PlayerHomes homes : players.values()) {
			for (Home home : homes.homes.values()) {
				result.add(new Entry(homes.name, home));
			}
		}

		result.sort(Comparator.comparing(Entry::playerName, String.CASE_INSENSITIVE_ORDER)
				.thenComparing(entry -> entry.home().name(), String.CASE_INSENSITIVE_ORDER));
		return result;
	}

	/**
	 * Finds a player id by the name stored when their last home was created, so that
	 * offline players can still be targeted by {@code /opdelhome}.
	 */
	public UUID findByName(String playerName) {
		for (Map.Entry<String, PlayerHomes> entry : players.entrySet()) {
			if (entry.getValue().name != null && entry.getValue().name.equalsIgnoreCase(playerName)) {
				try {
					return UUID.fromString(entry.getKey());
				} catch (IllegalArgumentException e) {
					Oxycarbide.LOGGER.warn("Skipping invalid player id {}", entry.getKey());
				}
			}
		}

		return null;
	}

	private static String normalize(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	/** One home together with the name of the player that owns it. */
	public record Entry(String playerName, Home home) {
	}

	/** Serialized shape of a single player's homes. */
	static final class PlayerHomes {
		String name = "";
		Map<String, Home> homes = new LinkedHashMap<>();
	}
}
