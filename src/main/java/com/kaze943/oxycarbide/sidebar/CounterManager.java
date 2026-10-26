package com.kaze943.oxycarbide.sidebar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Counters owned by oxycarbide, used by the {@code custom} line type of the sidebar.
 *
 * <p>They live in {@code <world>/oxycarbide_counters.json} and are edited with
 * {@code /oxycarbide scoreboard counter set|add}. Vanilla {@code /scoreboard} objectives
 * are not touched, so the two systems never collide. The file also remembers the last
 * known name of every player, so the sidebar can list offline players.</p>
 */
public final class CounterManager {
	private static final CounterManager INSTANCE = new CounterManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type LEGACY_TYPE = new TypeToken<Map<String, Map<String, Long>>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_counters.json";

	private final Data data = new Data();
	private Path file;

	private CounterManager() {
	}

	public static CounterManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		data.names.clear();
		data.counters.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			String text = Files.readString(file, StandardCharsets.UTF_8);
			JsonObject root = JsonParser.parseString(text).getAsJsonObject();

			if (root.has("counters")) {
				Data loaded = GSON.fromJson(root, Data.class);

				if (loaded != null) {
					if (loaded.names != null) {
						data.names.putAll(loaded.names);
					}

					if (loaded.counters != null) {
						data.counters.putAll(loaded.counters);
					}
				}
			} else {
				// Files written by the first version were a flat counter -> uuid -> value map.
				Map<String, Map<String, Long>> legacy = GSON.fromJson(text, LEGACY_TYPE);

				if (legacy != null) {
					data.counters.putAll(legacy);
				}
			}
		} catch (IOException | JsonSyntaxException | IllegalStateException e) {
			Oxycarbide.LOGGER.warn("Failed to read the counters from {}", file, e);
		}
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(data), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the counters to {}", file, e);
		}
	}

	public long get(String counter, UUID player) {
		Map<String, Long> values = data.counters.get(normalize(counter));
		return values == null ? 0L : values.getOrDefault(player.toString(), 0L);
	}

	public long set(String counter, UUID player, String playerName, long value) {
		rememberName(player, playerName);
		mapFor(counter).put(player.toString(), value);
		save();
		return value;
	}

	public long add(String counter, UUID player, String playerName, long amount) {
		rememberName(player, playerName);
		Map<String, Long> values = mapFor(counter);
		long result = values.getOrDefault(player.toString(), 0L) + amount;
		values.put(player.toString(), result);
		save();
		return result;
	}

	/** Every player that has a value for that counter, offline ones included. */
	public List<Entry> entries(String counter) {
		Map<String, Long> values = data.counters.get(normalize(counter));

		if (values == null || values.isEmpty()) {
			return List.of();
		}

		List<Entry> result = new ArrayList<>(values.size());

		for (Map.Entry<String, Long> entry : values.entrySet()) {
			UUID player;

			try {
				player = UUID.fromString(entry.getKey());
			} catch (IllegalArgumentException e) {
				continue;
			}

			String name = data.names.get(entry.getKey());
			result.add(new Entry(player, name == null ? entry.getKey() : name, entry.getValue()));
		}

		return result;
	}

	public Set<String> names() {
		return data.counters.keySet();
	}

	public static String normalize(String counter) {
		return counter == null || counter.isBlank() ? "points" : counter.trim();
	}

	private void rememberName(UUID player, String playerName) {
		if (playerName != null && !playerName.isBlank()) {
			data.names.put(player.toString(), playerName);
		}
	}

	private Map<String, Long> mapFor(String counter) {
		return data.counters.computeIfAbsent(normalize(counter), key -> new LinkedHashMap<>());
	}

	public void clear() {
		data.names.clear();
		data.counters.clear();
	}

	/** One counter value, with the last known name of its player. */
	public record Entry(UUID player, String name, long value) {
	}

	/** Shape of {@code oxycarbide_counters.json}. */
	private static final class Data {
		private Map<String, String> names = new LinkedHashMap<>();
		private Map<String, Map<String, Long>> counters = new LinkedHashMap<>();
	}
}
