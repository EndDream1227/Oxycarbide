package com.kaze943.oxycarbide.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kaze943.oxycarbide.Oxycarbide;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * Loads menus from {@code config/oxycarbide/menu/<id>.json}.
 *
 * <p>The file name (without {@code .json}) is the menu id used by {@code /menu <id>},
 * {@code /menu_default} and by the {@code menu} button action.</p>
 */
public final class MenuManager {
	private static final MenuManager INSTANCE = new MenuManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DIRECTORY = "oxycarbide/menu";
	private static final String EXAMPLE_ID = "example";
	private static final String ID_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

	private final Map<String, MenuDefinition> menus = new LinkedHashMap<>();
	private Path directory;

	private MenuManager() {
	}

	public static MenuManager get() {
		return INSTANCE;
	}

	public Path directory() {
		return directory;
	}

	public void load() {
		menus.clear();
		directory = FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY);

		try {
			Files.createDirectories(directory);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to create the menu directory {}", directory, e);
			return;
		}

		List<Path> files;

		try (Stream<Path> stream = Files.list(directory)) {
			files = stream
					.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
					.sorted()
					.toList();
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to list the menu directory {}", directory, e);
			return;
		}

		if (files.isEmpty()) {
			Path example = writeExample();

			if (example != null) {
				parse(example);
			}
		} else {
			for (Path file : files) {
				parse(file);
			}
		}

		Oxycarbide.LOGGER.info("Loaded {} menu(s) from {}", menus.size(), directory);
	}

	private void parse(Path path) {
		String fileName = path.getFileName().toString();
		String id = fileName.substring(0, fileName.length() - ".json".length());

		try {
			MenuDefinition menu = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), MenuDefinition.class);

			if (menu == null) {
				Oxycarbide.LOGGER.warn("Menu file {} is empty", path);
				return;
			}

			menu.id = id;

			if (menu.title == null || menu.title.isBlank()) {
				menu.title = id;
			}

			if (menu.buttons == null) {
				menu.buttons = new ArrayList<>();
			}

			menu.rows = Math.max(1, Math.min(6, menu.rows));
			menus.put(id.toLowerCase(Locale.ROOT), menu);
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read menu file {}", path, e);
		}
	}

	private Path writeExample() {
		MenuDefinition menu = new MenuDefinition();
		menu.title = "服务器菜单";
		menu.rows = 3;

		MenuButton spawn = new MenuButton();
		spawn.row = 2;
		spawn.column = 2;
		spawn.item = "minecraft:compass";
		spawn.enchanted = true;
		spawn.name = "回主城";
		spawn.lore = List.of("左键点击执行 /spawn");
		spawn.action = "command";
		spawn.value = "/spawn";

		MenuButton info = new MenuButton();
		info.row = 2;
		info.column = 4;
		info.item = "minecraft:paper";
		info.name = "说明";
		info.lore = List.of("这个文件位于 config/oxycarbide/menu/example.json", "照着它改就能做自己的菜单");
		info.action = "placeholder";

		MenuButton shop = new MenuButton();
		shop.row = 2;
		shop.column = 6;
		shop.item = "minecraft:emerald";
		shop.name = "打开商店";
		shop.lore = List.of("左键点击执行 /shop");
		shop.action = "command";
		shop.value = "/shop";

		menu.buttons.add(spawn);
		menu.buttons.add(info);
		menu.buttons.add(shop);

		Path path = directory.resolve(EXAMPLE_ID + ".json");

		try {
			Files.writeString(path, GSON.toJson(menu), StandardCharsets.UTF_8);
			return path;
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the example menu to {}", path, e);
			return null;
		}
	}

	/** @return {@code null} if there is no menu with that id */
	public MenuDefinition get(String id) {
		return id == null ? null : menus.get(id.toLowerCase(Locale.ROOT));
	}

	/**
	 * Creates an empty menu file. When {@code requestedId} is blank or {@code ~}, the id
	 * is generated randomly and looks like {@code menu_k3f9za}.
	 *
	 * @return the created menu, or {@code null} if the id is taken or the file could not
	 *         be written
	 */
	public MenuDefinition create(String requestedId) {
		Path dir = directory;

		if (dir == null) {
			return null;
		}

		String id = requestedId == null ? "" : requestedId.trim();

		if (id.isBlank() || "~".equals(id)) {
			id = generateId();
		}

		if (!isValidId(id) || menus.containsKey(id.toLowerCase(Locale.ROOT))
				|| Files.exists(dir.resolve(id + ".json"))) {
			return null;
		}

		MenuDefinition menu = new MenuDefinition();
		menu.id = id;
		menu.title = id;
		menu.rows = 3;

		Path path = dir.resolve(id + ".json");

		try {
			Files.writeString(path, GSON.toJson(menu), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the new menu to {}", path, e);
			return null;
		}

		menus.put(id.toLowerCase(Locale.ROOT), menu);
		return menu;
	}

	/** File names must stay readable: letters, digits, underscore and dash. */
	private static boolean isValidId(String id) {
		if (id.isEmpty() || id.length() > 32) {
			return false;
		}

		for (int index = 0; index < id.length(); index++) {
			char character = id.charAt(index);
			boolean allowed = character >= 'a' && character <= 'z'
					|| character >= 'A' && character <= 'Z'
					|| character >= '0' && character <= '9'
					|| character == '_' || character == '-';

			if (!allowed) {
				return false;
			}
		}

		return true;
	}

	private String generateId() {
		Path dir = directory;
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (int attempt = 0; attempt < 64; attempt++) {
			StringBuilder builder = new StringBuilder("menu_");

			for (int index = 0; index < 6; index++) {
				builder.append(ID_ALPHABET.charAt(random.nextInt(ID_ALPHABET.length())));
			}

			String candidate = builder.toString();

			if (!menus.containsKey(candidate.toLowerCase(Locale.ROOT))
					&& (dir == null || !Files.exists(dir.resolve(candidate + ".json")))) {
				return candidate;
			}
		}

		return "menu_" + System.currentTimeMillis();
	}

	public boolean contains(String id) {
		return id != null && menus.containsKey(id.toLowerCase(Locale.ROOT));
	}

	public List<MenuDefinition> all() {
		return new ArrayList<>(menus.values());
	}

	public int size() {
		return menus.size();
	}
}
