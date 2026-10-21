package com.kaze943.oxycarbide.treasury;

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
import java.util.Map;
import java.util.UUID;

/**
 * Player balances, stored in {@code <world>/oxycarbide_treasury.json}.
 */
public final class TreasuryManager {
	private static final TreasuryManager INSTANCE = new TreasuryManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Entry>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_treasury.json";

	private final Map<String, Entry> accounts = new LinkedHashMap<>();
	private Path file;

	private TreasuryManager() {
	}

	public static TreasuryManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		accounts.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, Entry> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				accounts.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read treasury accounts from {}", file, e);
		}
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(accounts, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write treasury accounts to {}", file, e);
		}
	}

	public long balance(UUID player) {
		Entry entry = accounts.get(player.toString());
		return entry == null ? 0L : entry.balance;
	}

	/**
	 * Registers a zero-balance account for a player, so that {@code /listreasury} also
	 * lists players who never received or spent anything.
	 */
	public void ensureAccount(UUID player, String name) {
		Entry entry = accounts.get(player.toString());

		if (entry == null) {
			entry = new Entry();
			entry.uuid = player.toString();
			entry.name = name;
			accounts.put(player.toString(), entry);
			save();
			return;
		}

		if (entry.name == null || entry.name.isBlank()) {
			entry.name = name;
			save();
		}
	}

	public void add(UUID player, String name, long amount) {
		Entry entry = accounts.computeIfAbsent(player.toString(), key -> new Entry());
		entry.name = name;
		entry.balance = Math.max(0L, entry.balance + amount);
		save();
	}

	/** @return {@code false} if the account does not exist or has not enough money */
	public boolean cut(UUID player, long amount) {
		Entry entry = accounts.get(player.toString());

		if (entry == null || entry.balance < amount) {
			return false;
		}

		entry.balance -= amount;
		save();
		return true;
	}

	/**
	 * Admin force-deduction: the balance is allowed to go negative.
	 *
	 * @return the new balance
	 */
	public long forceCut(UUID player, String name, long amount) {
		Entry entry = accounts.computeIfAbsent(player.toString(), key -> new Entry());

		if (entry.name == null || entry.name.isBlank()) {
			entry.name = name;
		}

		entry.balance -= amount;
		save();
		return entry.balance;
	}

	/** @return {@code false} if the sender has not enough money */
	public boolean transfer(UUID from, UUID to, String toName, long amount) {
		Entry source = accounts.get(from.toString());

		if (source == null || source.balance < amount) {
			return false;
		}

		Entry target = accounts.computeIfAbsent(to.toString(), key -> new Entry());

		if (target.name == null || target.name.isBlank()) {
			target.name = toName;
		}

		source.balance -= amount;
		target.balance += amount;
		save();
		return true;
	}

	/** Looks an account up by the name stored with it, for offline players. */
	public UUID findByName(String name) {
		for (Map.Entry<String, Entry> entry : accounts.entrySet()) {
			Entry account = entry.getValue();

			if (account.name != null && account.name.equalsIgnoreCase(name)) {
				try {
					return UUID.fromString(entry.getKey());
				} catch (IllegalArgumentException e) {
					Oxycarbide.LOGGER.warn("Skipping invalid treasury account id {}", entry.getKey());
				}
			}
		}

		return null;
	}

	public List<Entry> all() {
		List<Entry> result = new ArrayList<>();

		for (Map.Entry<String, Entry> entry : accounts.entrySet()) {
			// The map key is the owner UUID; keep it on the entry so commands can look up
			// that player's special currency balances too.
			entry.getValue().uuid = entry.getKey();
			result.add(entry.getValue());
		}

		result.sort(Comparator.comparing(entry -> entry.name, String.CASE_INSENSITIVE_ORDER));
		return result;
	}

	public void clear() {
		accounts.clear();
	}

	/** Serialized shape of one account. */
	public static final class Entry {
		public String uuid = "";
		public String name = "";
		public long balance;
	}
}
