package com.kaze943.oxycarbide.treasury;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kaze943.oxycarbide.Oxycarbide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Special currencies: named currencies with an exchange rate to the base coins, kept in
 * {@code <world>/oxycarbide_sptreasury.json}.
 *
 * <p>Balances are stored per currency and player, and the player name is remembered so
 * offline players can still be targeted by the admin commands.</p>
 */
public final class SpecialCurrencyManager {
	private static final SpecialCurrencyManager INSTANCE = new SpecialCurrencyManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "oxycarbide_sptreasury.json";

	private Data data = new Data();
	private Path file;

	private SpecialCurrencyManager() {
	}

	public static SpecialCurrencyManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		data = new Data();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Data loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Data.class);

			if (loaded != null) {
				data = loaded;
				data.normalize();
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read special currencies from {}", file, e);
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
			Oxycarbide.LOGGER.warn("Failed to write special currencies to {}", file, e);
		}
	}

	/** @return {@code false} if a currency with that name already exists */
	public boolean create(String name, double rate) {
		String key = key(name);

		if (data.currencies.containsKey(key)) {
			return false;
		}

		Currency currency = new Currency();
		currency.name = name;
		currency.rate = rate;
		data.currencies.put(key, currency);
		save();
		return true;
	}

	public boolean exists(String name) {
		return data.currencies.containsKey(key(name));
	}

	public Currency currency(String name) {
		return data.currencies.get(key(name));
	}

	public long balance(String name, UUID player) {
		Map<String, Long> balances = data.balances.get(key(name));
		return balances == null ? 0L : balances.getOrDefault(player.toString(), 0L);
	}

	public void add(String name, UUID player, String playerName, long amount) {
		String key = key(name);
		Map<String, Long> balances = data.balances.computeIfAbsent(key, k -> new LinkedHashMap<>());
		balances.merge(player.toString(), amount, Long::sum);
		data.names.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(player.toString(), playerName);
		save();
	}

	/**
	 * Admin force-deduction: the balance is allowed to go negative.
	 *
	 * @return the new balance
	 */
	public long forceCut(String name, UUID player, String playerName, long amount) {
		String key = key(name);
		Map<String, Long> balances = data.balances.computeIfAbsent(key, k -> new LinkedHashMap<>());
		long newBalance = balances.getOrDefault(player.toString(), 0L) - amount;
		balances.put(player.toString(), newBalance);
		data.names.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(player.toString(), playerName);
		save();
		return newBalance;
	}

	/** @return {@code false} if the sender has not enough of that currency */
	public boolean transfer(String name, UUID from, UUID to, String toName, long amount) {
		String key = key(name);
		Map<String, Long> balances = data.balances.computeIfAbsent(key, k -> new LinkedHashMap<>());
		long balance = balances.getOrDefault(from.toString(), 0L);

		if (balance < amount) {
			return false;
		}

		balances.put(from.toString(), balance - amount);
		balances.merge(to.toString(), amount, Long::sum);
		data.names.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(to.toString(), toName);
		save();
		return true;
	}

	public UUID findByName(String name, String playerName) {
		Map<String, String> names = data.names.get(key(name));

		if (names == null) {
			return null;
		}

		for (Map.Entry<String, String> entry : names.entrySet()) {
			if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(playerName)) {
				try {
					return UUID.fromString(entry.getKey());
				} catch (IllegalArgumentException e) {
					Oxycarbide.LOGGER.warn("Skipping invalid special currency account id {}", entry.getKey());
				}
			}
		}

		return null;
	}

	public List<Currency> currencies() {
		return new ArrayList<>(data.currencies.values());
	}

	public void clear() {
		data = new Data();
	}

	private static String key(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	/** Serialized shape of one special currency. */
	public static final class Currency {
		public String name = "";
		public double rate = 1.0;
	}

	/** Whole-file shape. */
	public static final class Data {
		public Map<String, Currency> currencies = new LinkedHashMap<>();
		public Map<String, Map<String, Long>> balances = new LinkedHashMap<>();
		public Map<String, Map<String, String>> names = new LinkedHashMap<>();

		void normalize() {
			if (currencies == null) {
				currencies = new LinkedHashMap<>();
			}

			if (balances == null) {
				balances = new LinkedHashMap<>();
			}

			if (names == null) {
				names = new LinkedHashMap<>();
			}
		}
	}
}
