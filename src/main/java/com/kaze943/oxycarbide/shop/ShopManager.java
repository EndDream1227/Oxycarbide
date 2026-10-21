package com.kaze943.oxycarbide.shop;

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
import java.util.stream.Stream;

/**
 * Loads shop GUIs from {@code config/oxycarbide/shop/config/<id>.json}.
 *
 * <p>The file name (without {@code .json}) is the shop id used by {@code /shop <id>} and
 * by the {@code redirect} trade type.</p>
 */
public final class ShopManager {
	private static final ShopManager INSTANCE = new ShopManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DIRECTORY = "oxycarbide/shop/config";
	private static final String EXAMPLE_ID = "example";

	private final Map<String, ShopDefinition> shops = new LinkedHashMap<>();
	private Path directory;

	private ShopManager() {
	}

	public static ShopManager get() {
		return INSTANCE;
	}

	public Path directory() {
		return directory;
	}

	public void load() {
		shops.clear();
		directory = FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY);

		try {
			Files.createDirectories(directory);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to create the shop directory {}", directory, e);
			return;
		}

		List<Path> files;

		try (Stream<Path> stream = Files.list(directory)) {
			files = stream
					.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
					.sorted()
					.toList();
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to list the shop directory {}", directory, e);
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

		Oxycarbide.LOGGER.info("Loaded {} shop GUI(s) from {}", shops.size(), directory);
	}

	private void parse(Path path) {
		String fileName = path.getFileName().toString();
		String id = fileName.substring(0, fileName.length() - ".json".length());

		try {
			ShopDefinition shop = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), ShopDefinition.class);

			if (shop == null) {
				Oxycarbide.LOGGER.warn("Shop file {} is empty", path);
				return;
			}

			shop.id = id;

			if (shop.title == null || shop.title.isBlank()) {
				shop.title = id;
			}

			if (shop.items == null) {
				shop.items = new ArrayList<>();
			}

			shop.rows = Math.max(1, Math.min(6, shop.rows));
			shops.put(id.toLowerCase(Locale.ROOT), shop);
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read shop file {}", path, e);
		}
	}

	private Path writeExample() {
		ShopDefinition shop = new ShopDefinition();
		shop.title = "示例商店";
		shop.rows = 3;

		ShopItemDefinition buy = new ShopItemDefinition();
		buy.row = 2;
		buy.column = 2;
		buy.display = "minecraft:diamond";
		buy.enchanted = true;
		buy.name = "钻石";
		buy.lore = List.of("示例：左键购买");
		buy.type = "buy";
		buy.item = "minecraft:diamond";
		buy.amount = 1;
		buy.price = 100;

		ShopItemDefinition sell = new ShopItemDefinition();
		sell.row = 2;
		sell.column = 4;
		sell.display = "minecraft:emerald";
		sell.name = "绿宝石";
		sell.lore = List.of("示例：左键出售");
		sell.type = "sell";
		sell.item = "minecraft:emerald";
		sell.amount = 1;
		sell.price = 50;

		shop.items.add(buy);
		shop.items.add(sell);

		Path path = directory.resolve(EXAMPLE_ID + ".json");

		try {
			Files.writeString(path, GSON.toJson(shop), StandardCharsets.UTF_8);
			return path;
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the example shop to {}", path, e);
			return null;
		}
	}

	/** @return {@code null} if there is no shop with that id */
	public ShopDefinition get(String id) {
		return shops.get(id.toLowerCase(Locale.ROOT));
	}

	/**
	 * Creates an empty shop file. When {@code requestedId} is blank or {@code ~}, a free
	 * id of the form {@code shop1}, {@code shop2}, ... is generated.
	 *
	 * @return the created shop, or {@code null} if the id is taken or the file could not
	 *         be written
	 */
	public ShopDefinition create(String title, String requestedId) {
		Path dir = directory;

		if (dir == null) {
			return null;
		}

		String id = requestedId;

		if (id == null || id.isBlank() || "~".equals(id)) {
			id = generateId();
		}

		if (shops.containsKey(id.toLowerCase(Locale.ROOT))) {
			return null;
		}

		ShopDefinition shop = new ShopDefinition();
		shop.id = id;
		shop.title = title == null || title.isBlank() ? id : title;
		shop.rows = 6;

		Path path = dir.resolve(id + ".json");

		try {
			Files.writeString(path, GSON.toJson(shop), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the new shop to {}", path, e);
			return null;
		}

		shops.put(id.toLowerCase(Locale.ROOT), shop);
		return shop;
	}

	private String generateId() {
		int number = shops.size() + 1;
		String candidate = "shop" + number;

		while (shops.containsKey(candidate.toLowerCase(Locale.ROOT))) {
			number++;
			candidate = "shop" + number;
		}

		return candidate;
	}

	public boolean contains(String id) {
		return shops.containsKey(id.toLowerCase(Locale.ROOT));
	}

	public List<ShopDefinition> all() {
		return new ArrayList<>(shops.values());
	}

	public int size() {
		return shops.size();
	}
}
