package com.kaze943.oxycarbide.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import com.kaze943.oxycarbide.shop.ShopItemDefinition;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Player listings of the global market plus its editable layout.
 *
 * <p>Listings live in {@code <world>/oxycarbide_market.json} and the GUI frame in
 * {@code config/oxycarbide/globalshop_default.json}.</p>
 */
public final class MarketManager {
	private static final MarketManager INSTANCE = new MarketManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type LIST_TYPE = new TypeToken<List<MarketListing>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_market.json";
	private static final String PENDING_FILE = "oxycarbide_market_pending.json";
	private static final String LAYOUT_FILE = "globalshop_default.json";
	private static final Type PENDING_TYPE = new TypeToken<List<PendingReturn>>() {
	}.getType();
	private static final Random RANDOM = new Random();

	/** Fallback listing duration, in minutes; the config value is used by /sell. */
	public static final int DEFAULT_MINUTES = 180;

	private final List<MarketListing> listings = new ArrayList<>();
	private final List<PendingReturn> pending = new ArrayList<>();
	private MarketLayout layout = new MarketLayout();
	private Path file;
	private Path pendingFile;
	private Path layoutPath;

	private MarketManager() {
	}

	public static MarketManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		listings.clear();
		pending.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
		pendingFile = server.getWorldPath(LevelResource.ROOT).resolve(PENDING_FILE);
		layoutPath = FabricLoader.getInstance().getConfigDir().resolve("oxycarbide").resolve(LAYOUT_FILE);

		if (Files.exists(file)) {
			try {
				List<MarketListing> loaded = GSON.fromJson(
						Files.readString(file, StandardCharsets.UTF_8), LIST_TYPE);

				if (loaded != null) {
					listings.addAll(loaded);
				}
			} catch (IOException | JsonSyntaxException e) {
				Oxycarbide.LOGGER.warn("Failed to read the market from {}", file, e);
			}
		}

		if (Files.exists(pendingFile)) {
			try {
				List<PendingReturn> loaded = GSON.fromJson(
						Files.readString(pendingFile, StandardCharsets.UTF_8), PENDING_TYPE);

				if (loaded != null) {
					pending.addAll(loaded);
				}
			} catch (IOException | JsonSyntaxException e) {
				Oxycarbide.LOGGER.warn("Failed to read pending market returns from {}", pendingFile, e);
			}
		}

		loadLayout();
		prune();
	}

	private void loadLayout() {
		layout = new MarketLayout();

		if (!Files.exists(layoutPath)) {
			writeDefaultLayout();
			return;
		}

		try {
			MarketLayout loaded = GSON.fromJson(
					Files.readString(layoutPath, StandardCharsets.UTF_8), MarketLayout.class);

			if (loaded != null) {
				layout = loaded;
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read the market layout from {}", layoutPath, e);
		}

		if (layout.title == null || layout.title.isBlank()) {
			layout.title = "世界市场";
		}

		layout.rows = Math.max(2, Math.min(6, layout.rows));

		if (layout.previous == null) {
			layout.previous = new ShopItemDefinition();
		}

		if (layout.next == null) {
			layout.next = new ShopItemDefinition();
		}

		if (layout.frame == null) {
			layout.frame = new ArrayList<>();
		}

		// Frame entries may only redirect or do nothing.
		for (ShopItemDefinition item : layout.frame) {
			String type = item.typeLower();

			if (!"redirect".equals(type) && !"placeholder".equals(type)) {
				item.type = "placeholder";
			}
		}
	}

	private void writeDefaultLayout() {
		MarketLayout defaults = new MarketLayout();
		defaults.title = "世界市场";
		defaults.rows = 6;

		defaults.previous.name = "上一页";
		defaults.previous.display = "minecraft:paper";
		defaults.previous.lore = List.of("点击查看上一页");

		defaults.next.name = "下一页";
		defaults.next.display = "minecraft:paper";
		defaults.next.lore = List.of("点击查看下一页");

		try {
			Files.createDirectories(layoutPath.getParent());
			Files.writeString(layoutPath, GSON.toJson(defaults), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the default market layout to {}", layoutPath, e);
		}
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(listings, LIST_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the market to {}", file, e);
		}
	}

	public void savePending() {
		if (pendingFile == null) {
			return;
		}

		try {
			Files.createDirectories(pendingFile.getParent());
			Files.writeString(pendingFile, GSON.toJson(pending, PENDING_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write pending market returns to {}", pendingFile, e);
		}
	}

	/** Stores an item that has to be handed back to an offline player. */
	public void addPending(String owner, String itemName, com.google.gson.JsonElement item) {
		PendingReturn entry = new PendingReturn();
		entry.owner = owner;
		entry.itemName = itemName;
		entry.item = item;
		pending.add(entry);
		savePending();
	}

	/** Returns (and forgets) every pending item of that player. */
	public List<PendingReturn> takePending(java.util.UUID owner) {
		List<PendingReturn> result = new ArrayList<>();
		String id = owner.toString();
		pending.removeIf(entry -> {
			if (id.equals(entry.owner)) {
				result.add(entry);
				return true;
			}

			return false;
		});

		if (!result.isEmpty()) {
			savePending();
		}

		return result;
	}

	public MarketLayout layout() {
		return layout;
	}

	public List<MarketListing> all() {
		return new ArrayList<>(listings);
	}

	public MarketListing find(String id) {
		if (id == null) {
			return null;
		}

		for (MarketListing listing : listings) {
			if (listing.id.equals(id)) {
				return listing;
			}
		}

		return null;
	}

	public void add(MarketListing listing) {
		listings.add(listing);
		save();
	}

	public MarketListing remove(String id) {
		MarketListing listing = find(id);

		if (listing != null) {
			listings.remove(listing);
			save();
		}

		return listing;
	}

	public boolean isExpired(MarketListing listing) {
		return listing.expiresAt > 0 && listing.expiresAt <= System.currentTimeMillis();
	}

	/** Expired listings are removed and their items queued for the seller. */
	public boolean prune() {
		List<MarketListing> expired = new ArrayList<>();

		for (MarketListing listing : listings) {
			if (isExpired(listing)) {
				expired.add(listing);
			}
		}

		if (expired.isEmpty()) {
			return false;
		}

		for (MarketListing listing : expired) {
			listings.remove(listing);
			addPending(listing.seller, listing.itemName, listing.item);
		}

		save();
		return true;
	}

	public String newId() {
		while (true) {
			long value = 1_000_000_000L + (long) (RANDOM.nextDouble() * 9_000_000_000L);
			String id = Long.toString(value);

			if (find(id) == null) {
				return id;
			}
		}
	}

	public int rows() {
		return Math.max(2, Math.min(6, layout.rows));
	}

	/** Slots between the top and bottom frame rows. */
	public int listingSlots() {
		return (rows() - 2) * 9;
	}

	public int firstListingSlot() {
		return 9;
	}

	public int prevSlot() {
		return (rows() - 1) * 9;
	}

	public int nextSlot() {
		return (rows() - 1) * 9 + 8;
	}

	public int pageCount() {
		int slots = listingSlots();

		if (slots <= 0) {
			return 1;
		}

		int count = (int) Math.ceil(listings.size() / (double) slots);
		return Math.max(1, count);
	}

	public List<MarketListing> page(int page) {
		int slots = listingSlots();
		int start = Math.max(0, (page - 1) * slots);
		int end = Math.min(listings.size(), start + slots);

		if (start >= end) {
			return List.of();
		}

		return listings.subList(start, end);
	}

	public static String currencyLabel(String currency) {
		if (currency == null || currency.isBlank()
				|| "normal".equalsIgnoreCase(currency)) {
			return com.kaze943.oxycarbide.config.OxycarbideConfig.get().getCurrencyName();
		}

		return currency;
	}

	public static String idLower(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}
}
