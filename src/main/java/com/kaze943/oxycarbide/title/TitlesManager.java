package com.kaze943.oxycarbide.title;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kaze943.oxycarbide.Oxycarbide;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.nickname.NicknameManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Titles: the little prefix shown before a player name in the chat and above the head.
 *
 * <p>The definitions are JSON ({@code config/oxycarbide/title/titles.json}); which player
 * owns and wears which title is stored in {@code <world>/oxycarbide_titles.json}. Nothing
 * here uses the vanilla team feature: the chat prefix comes from a display name mixin and
 * the name tag is the entity's custom name.</p>
 */
public final class TitlesManager {
	private static final TitlesManager INSTANCE = new TitlesManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DIRECTORY = "oxycarbide/title";
	private static final String DEFINITION_FILE = "titles.json";
	private static final String PLAYER_FILE = "oxycarbide_titles.json";
	private static final int MAX_TEXT_LENGTH = 32;
	private static final int MAX_ID_LENGTH = 32;
	private static final String ID_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

	private final Map<String, TitleDefinition> titles = new LinkedHashMap<>();
	private final Map<String, Component> rendered = new HashMap<>();
	private final Map<String, Set<String>> owned = new HashMap<>();
	private final Map<String, String> worn = new HashMap<>();
	private Path definitionFile;
	private Path playerFile;

	private TitlesManager() {
	}

	public static TitlesManager get() {
		return INSTANCE;
	}

	/** Re-reads both files; {@code server} may be {@code null} during startup. */
	public void load(MinecraftServer server) {
		loadDefinitions();
		loadPlayers(server);
		refreshAll(server);
	}

	public void loadDefinitions() {
		titles.clear();
		rendered.clear();
		Path directory = FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY);
		definitionFile = directory.resolve(DEFINITION_FILE);

		try {
			Files.createDirectories(directory);

			if (!Files.exists(definitionFile)) {
				Files.writeString(definitionFile, EXAMPLE, StandardCharsets.UTF_8);
			}

			File loaded = GSON.fromJson(Files.readString(definitionFile, StandardCharsets.UTF_8), File.class);

			if (loaded != null && loaded.titles != null) {
				for (TitleDefinition title : loaded.titles) {
					add(title);
				}
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read the titles from {}", definitionFile, e);
		}
	}

	public void loadPlayers(MinecraftServer server) {
		owned.clear();
		worn.clear();

		if (server == null) {
			return;
		}

		playerFile = server.getWorldPath(LevelResource.ROOT).resolve(PLAYER_FILE);

		if (!Files.exists(playerFile)) {
			return;
		}

		try {
			PlayerData data = GSON.fromJson(Files.readString(playerFile, StandardCharsets.UTF_8), PlayerData.class);

			if (data == null) {
				return;
			}

			if (data.owned != null) {
				data.owned.forEach((player, ids) -> {
					if (player != null && ids != null) {
						Set<String> set = new LinkedHashSet<>();

						for (String id : ids) {
							if (id != null) {
								set.add(id.toLowerCase(Locale.ROOT));
							}
						}

						owned.put(player, set);
					}
				});
			}

			if (data.worn != null) {
				data.worn.forEach((player, id) -> {
					if (player != null && id != null) {
						worn.put(player, id.toLowerCase(Locale.ROOT));
					}
				});
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read the player titles from {}", playerFile, e);
		}
	}

	public void save() {
		if (playerFile == null) {
			return;
		}

		PlayerData data = new PlayerData();
		owned.forEach((player, ids) -> data.owned.put(player, new ArrayList<>(ids)));
		data.worn.putAll(worn);

		try {
			Files.createDirectories(playerFile.getParent());
			Files.writeString(playerFile, GSON.toJson(data), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the player titles to {}", playerFile, e);
		}
	}

	public void saveDefinitions() {
		if (definitionFile == null) {
			return;
		}

		File data = new File();
		data.titles = new ArrayList<>(titles.values());

		try {
			Files.createDirectories(definitionFile.getParent());
			Files.writeString(definitionFile, GSON.toJson(data), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the titles to {}", definitionFile, e);
		}
	}

	/**
	 * Creates a title. {@code requestedId} may be {@code ~} to generate one.
	 *
	 * @return the new title, or {@code null} when the text is empty or the id is taken
	 */
	public TitleDefinition create(String text, String color, String requestedId) {
		String value = text == null ? "" : text.trim();

		if (value.isEmpty()) {
			return null;
		}

		if (value.length() > MAX_TEXT_LENGTH) {
			value = value.substring(0, MAX_TEXT_LENGTH);
		}

		String id = requestedId == null ? "" : requestedId.trim();

		if (id.isBlank() || "~".equals(id)) {
			id = generateId();
		}

		if (!isValidId(id) || contains(id)) {
			return null;
		}

		TitleDefinition title = new TitleDefinition();
		title.id = id.toLowerCase(Locale.ROOT);
		title.text = value;
		title.color = color == null || color.isBlank() ? "white" : color.trim();
		add(title);
		saveDefinitions();
		return title;
	}

	private void add(TitleDefinition title) {
		if (title == null || title.id == null || title.text == null) {
			return;
		}

		String id = title.id.trim().toLowerCase(Locale.ROOT);

		if (!isValidId(id) || title.text.isBlank()) {
			Oxycarbide.LOGGER.warn("Skipping invalid title id/text: {} / {}", title.id, title.text);
			return;
		}

		title.id = id;
		titles.put(id, title);
		rendered.put(id, TitleColors.render(title.text, title.color));
	}

	public boolean isValidId(String id) {
		if (id == null || id.isEmpty() || id.length() > MAX_ID_LENGTH) {
			return false;
		}

		for (int index = 0; index < id.length(); index++) {
			char character = id.charAt(index);
			boolean allowed = character >= 'a' && character <= 'z'
					|| character >= 'A' && character <= 'Z'
					|| character >= '0' && character <= '9';

			if (!allowed) {
				return false;
			}
		}

		return true;
	}

	private String generateId() {
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (int attempt = 0; attempt < 64; attempt++) {
			StringBuilder builder = new StringBuilder("t");

			for (int index = 0; index < 6; index++) {
				builder.append(ID_ALPHABET.charAt(random.nextInt(ID_ALPHABET.length())));
			}

			String candidate = builder.toString();

			if (!contains(candidate)) {
				return candidate;
			}
		}

		return "t" + System.currentTimeMillis();
	}

	public TitleDefinition get(String id) {
		return id == null ? null : titles.get(id.trim().toLowerCase(Locale.ROOT));
	}

	public boolean contains(String id) {
		return get(id) != null;
	}

	public List<TitleDefinition> all() {
		return new ArrayList<>(titles.values());
	}

	public List<String> ids() {
		return new ArrayList<>(titles.keySet());
	}

	/** Gives a title to a player; the caller checks that the title exists. */
	public void grant(UUID player, String id) {
		owned.computeIfAbsent(player.toString(), key -> new LinkedHashSet<>())
				.add(id.toLowerCase(Locale.ROOT));
		save();
	}

	public boolean owns(UUID player, String id) {
		Set<String> ids = owned.get(player.toString());
		return ids != null && ids.contains(id.toLowerCase(Locale.ROOT));
	}

	public List<String> ownedIds(UUID player) {
		Set<String> ids = owned.get(player.toString());
		return ids == null ? List.of() : new ArrayList<>(ids);
	}

	/** Makes the player wear a title they own. */
	public boolean wear(UUID player, String id) {
		if (!contains(id) || !owns(player, id)) {
			return false;
		}

		worn.put(player.toString(), id.toLowerCase(Locale.ROOT));
		save();
		return true;
	}

	public void clearWorn(UUID player) {
		if (worn.remove(player.toString()) != null) {
			save();
		}
	}

	public String wornId(UUID player) {
		return worn.get(player.toString());
	}

	/**
	 * The title component followed by the player name, or {@code null} when no title is
	 * worn. Called from the display name mixin, so it must stay cheap.
	 */
	public Component decorate(Player player, Component nickname) {
		if (!OxycarbideConfig.get().isTitlesEnabled()) {
			return null;
		}

		String id = worn.get(player.getUUID().toString());

		if (id == null) {
			return null;
		}

		Component title = rendered.get(id);
		TitleDefinition definition = titles.get(id);

		if (title == null || definition == null) {
			return null;
		}

		Component base = nickname != null ? nickname : player.getName();
		return Component.empty().append(title).append(Component.literal(" ")).append(base);
	}

	/** The full display name, used for name tags. */
	public Component displayName(ServerPlayer player) {
		Component nickname = NicknameManager.get().displayName(player);
		Component decorated = decorate(player, nickname);

		if (decorated != null) {
			return decorated;
		}

		return nickname != null ? nickname : player.getName();
	}

	/** Pushes the title into the name tag above the player's head. */
	public void refreshNameTag(ServerPlayer player) {
		Component nickname = NicknameManager.get().displayName(player);
		Component decorated = decorate(player, nickname);

		if (decorated == null && nickname == null) {
			player.setCustomName(null);
			player.setCustomNameVisible(false);
			return;
		}

		player.setCustomName(decorated != null ? decorated : nickname);
		player.setCustomNameVisible(true);
	}

	/** Re-applies the name tags after a reload, e.g. when a colour changed. */
	public void refreshAll(MinecraftServer server) {
		if (server == null) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			refreshNameTag(player);
		}
	}

	public void clear() {
		titles.clear();
		rendered.clear();
		owned.clear();
		worn.clear();
	}

	/** Shape of {@code titles.json}. */
	private static final class File {
		private List<TitleDefinition> titles = new ArrayList<>();
	}

	/** Shape of {@code oxycarbide_titles.json}. */
	private static final class PlayerData {
		private Map<String, List<String>> owned = new LinkedHashMap<>();
		private Map<String, String> worn = new LinkedHashMap<>();
	}

	private static final String EXAMPLE = """
			{
			  "titles": [
			    { "id": "vip", "text": "[VIP]", "color": "gold" },
			    { "id": "member", "text": "[MEMBER]", "color": "green" },
			    { "id": "rainbow", "text": "[RAINBOW]", "color": "#ff0000,#ffff00,#00ff00,#00ffff,#0000ff,#ff00ff" }
			  ]
			}
			""";
}
