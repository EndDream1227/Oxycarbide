package com.kaze943.oxycarbide.warp;

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
 * Stores every warp and keeps them in {@code <world>/oxycarbide_warps.json}.
 *
 * <p>Keys of the map are the lower-cased warp names, so {@code /warp Spawn} and
 * {@code /warp spawn} refer to the same warp.</p>
 */
public final class WarpManager {
	private static final WarpManager INSTANCE = new WarpManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Warp>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_warps.json";

	private final Map<String, Warp> warps = new LinkedHashMap<>();
	private Path file;

	private WarpManager() {
	}

	public static WarpManager get() {
		return INSTANCE;
	}

	/** (Re)loads the warps of the world that is currently running. */
	public void load(MinecraftServer server) {
		warps.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, Warp> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				warps.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read warps from {}", file, e);
		}
	}

	/** Writes the current state to disk. Safe to call often, the file is small. */
	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(warps, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write warps to {}", file, e);
		}
	}

	public Warp get(String name) {
		return warps.get(normalize(name));
	}

	/** @return {@code false} if a warp with that name already exists */
	public boolean put(Warp warp) {
		String key = normalize(warp.name);

		if (warps.containsKey(key)) {
			return false;
		}

		warps.put(key, warp);
		return true;
	}

	/** @return the removed warp, or {@code null} if there was no warp with that name */
	public Warp remove(String name) {
		return warps.remove(normalize(name));
	}

	/** @return {@code false} if the old warp does not exist or the new name is taken */
	public boolean rename(String oldName, String newName) {
		String oldKey = normalize(oldName);
		String newKey = normalize(newName);
		Warp warp = warps.get(oldKey);

		if (warp == null || warps.containsKey(newKey)) {
			return false;
		}

		warps.remove(oldKey);
		warp.name = newName;
		warps.put(newKey, warp);
		return true;
	}

	/** Every warp, sorted by name. */
	public List<Warp> all() {
		List<Warp> result = new ArrayList<>(warps.values());
		result.sort(Comparator.comparing(Warp::name, String.CASE_INSENSITIVE_ORDER));
		return result;
	}

	public int countOwnedBy(UUID owner) {
		String ownerId = owner.toString();
		int count = 0;

		for (Warp warp : warps.values()) {
			if (ownerId.equals(warp.owner)) {
				count++;
			}
		}

		return count;
	}

	private static String normalize(String name) {
		return name.toLowerCase(Locale.ROOT);
	}
}
