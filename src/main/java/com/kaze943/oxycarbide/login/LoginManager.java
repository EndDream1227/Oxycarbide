package com.kaze943.oxycarbide.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Account storage, login sessions and the 24h request log.
 *
 * <p>Accounts are persisted in {@code <world>/oxycarbide_accounts.json}; sessions and the
 * request log live in memory only.</p>
 */
public final class LoginManager {
	public enum EditResult {
		OK,
		NOT_FOUND,
		BAD_PASSWORD,
		NAME_TAKEN
	}

	/** One {@code /login} attempt, kept for {@code /loginrequest}. */
	public record LoginRequest(long time, String player, String username, boolean success) {
	}

	private static final LoginManager INSTANCE = new LoginManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Account>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_accounts.json";
	private static final long REQUEST_WINDOW_MILLIS = 24L * 60L * 60L * 1000L;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final Map<String, Account> accounts = new LinkedHashMap<>();
	private final Set<UUID> authenticated = new HashSet<>();
	private final Map<UUID, Session> sessions = new HashMap<>();
	private final List<LoginRequest> requests = new ArrayList<>();

	private Path file;
	private long serverStartMillis;

	private LoginManager() {
	}

	public static LoginManager get() {
		return INSTANCE;
	}

	// ---------------------------------------------------------------- storage

	public void load(MinecraftServer server) {
		accounts.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, Account> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				accounts.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read login accounts from {}", file, e);
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
			Oxycarbide.LOGGER.warn("Failed to write login accounts to {}", file, e);
		}
	}

	public void markStarted() {
		serverStartMillis = System.currentTimeMillis();
		requests.clear();
	}

	// ---------------------------------------------------------------- accounts

	public boolean hasAccount(UUID player) {
		return accounts.containsKey(player.toString());
	}

	public Account accountOf(UUID player) {
		return accounts.get(player.toString());
	}

	public Optional<Account> findByUsername(String username) {
		for (Account account : accounts.values()) {
			if (account.username.equalsIgnoreCase(username)) {
				return Optional.of(account);
			}
		}

		return Optional.empty();
	}

	public Optional<Account> findByPlayerName(String playerName) {
		for (Account account : accounts.values()) {
			if (account.playerName.equalsIgnoreCase(playerName)) {
				return Optional.of(account);
			}
		}

		return Optional.empty();
	}

	/** @return {@code false} if the username is already taken */
	public boolean register(UUID player, String playerName, String username, String password) {
		if (findByUsername(username).isPresent()) {
			return false;
		}

		Account account = new Account();
		account.username = username;
		account.playerName = playerName;
		account.uuid = player.toString();
		account.salt = newSalt();
		account.hash = hash(account.salt, password);
		accounts.put(player.toString(), account);
		save();
		return true;
	}

	public boolean authenticate(UUID player, String username, String password, boolean simple) {
		Account account = simple ? accountOf(player) : findByUsername(username).orElse(null);

		if (account == null) {
			return false;
		}

		if (!simple && !account.uuid.equals(player.toString())) {
			return false;
		}

		return account.hash.equals(hash(account.salt, password));
	}

	public EditResult editNormal(String oldUsername, String oldPassword, String newUsername, String newPassword) {
		Account account = findByUsername(oldUsername).orElse(null);

		if (account == null) {
			return EditResult.NOT_FOUND;
		}

		if (!account.hash.equals(hash(account.salt, oldPassword))) {
			return EditResult.BAD_PASSWORD;
		}

		Optional<Account> existing = findByUsername(newUsername);

		if (existing.isPresent() && existing.get() != account) {
			return EditResult.NAME_TAKEN;
		}

		account.username = newUsername;
		account.salt = newSalt();
		account.hash = hash(account.salt, newPassword);
		save();
		return EditResult.OK;
	}

	public EditResult editSimple(String playerName, String oldPassword, String newPassword) {
		Account account = findByPlayerName(playerName).orElse(null);

		if (account == null) {
			return EditResult.NOT_FOUND;
		}

		if (!account.hash.equals(hash(account.salt, oldPassword))) {
			return EditResult.BAD_PASSWORD;
		}

		account.salt = newSalt();
		account.hash = hash(account.salt, newPassword);
		save();
		return EditResult.OK;
	}

	public List<Account> accounts() {
		return new ArrayList<>(accounts.values());
	}

	// ---------------------------------------------------------------- sessions

	public void onJoin(ServerPlayer player, MinecraftServer server) {
		if (!OxycarbideConfig.get().isLoginEnabled()) {
			return;
		}

		UUID uuid = player.getUUID();
		authenticated.remove(uuid);

		OxycarbideConfig config = OxycarbideConfig.get();
		boolean existing = hasAccount(uuid);
		int ticks = existing ? config.getLoginTime() : config.getLoginTimeFirst();
		int seconds = Math.max(1, (ticks + 19) / 20);

		ServerLevel level = (ServerLevel) player.level();
		sessions.put(uuid, new Session(level, player.getX(), player.getY(), player.getZ(),
				player.getYRot(), player.getXRot(), server.getTickCount() + ticks));

		if (existing) {
			player.sendSystemMessage(LoginTexts.component("prompt_login", seconds));
		} else {
			player.sendSystemMessage(LoginTexts.component("welcome"));
			player.sendSystemMessage(LoginTexts.component("prompt_register", seconds));
		}
	}

	public void onDisconnect(ServerPlayer player) {
		sessions.remove(player.getUUID());
		authenticated.remove(player.getUUID());
	}

	public void markAuthenticated(UUID player) {
		sessions.remove(player);
		authenticated.add(player);
	}

	public boolean isAuthenticated(UUID player) {
		return authenticated.contains(player);
	}

	/** True while the player is online but has not completed the login yet. */
	public boolean isLocked(ServerPlayer player) {
		if (!OxycarbideConfig.get().isLoginEnabled()) {
			return false;
		}

		UUID uuid = player.getUUID();
		return sessions.containsKey(uuid) && !authenticated.contains(uuid);
	}

	public boolean isLocked(Player player) {
		return player instanceof ServerPlayer serverPlayer && !player.level().isClientSide() && isLocked(serverPlayer);
	}

	/** Keeps unauthenticated players frozen and kicks them when their time runs out. */
	public void tick(MinecraftServer server) {
		if (sessions.isEmpty() || !OxycarbideConfig.get().isLoginEnabled()) {
			return;
		}

		Iterator<Map.Entry<UUID, Session>> iterator = sessions.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, Session> entry = iterator.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

			if (player == null || authenticated.contains(entry.getKey())) {
				iterator.remove();
				continue;
			}

			Session session = entry.getValue();

			if (server.getTickCount() >= session.deadlineTick()) {
				iterator.remove();
				player.connection.disconnect(Component.translatable("oxycarbide.login.timeout"));
				continue;
			}

			// Lock position, view and momentum.
			player.setDeltaMovement(0.0, 0.0, 0.0);

			if (Math.abs(player.getX() - session.x()) > 0.001
					|| Math.abs(player.getY() - session.y()) > 0.001
					|| Math.abs(player.getZ() - session.z()) > 0.001
					|| Math.abs(player.getYRot() - session.yaw()) > 0.01
					|| Math.abs(player.getXRot() - session.pitch()) > 0.01) {
				player.connection.teleport(session.x(), session.y(), session.z(), session.yaw(), session.pitch());
			}
		}
	}

	// ---------------------------------------------------------------- requests

	public void recordRequest(String playerName, String username, boolean success) {
		long now = System.currentTimeMillis();
		requests.add(new LoginRequest(now, playerName, username, success));

		long cutoff = Math.max(serverStartMillis, now - REQUEST_WINDOW_MILLIS);
		requests.removeIf(request -> request.time() < cutoff);
	}

	public List<LoginRequest> requests() {
		long cutoff = Math.max(serverStartMillis, System.currentTimeMillis() - REQUEST_WINDOW_MILLIS);
		List<LoginRequest> result = new ArrayList<>();

		for (LoginRequest request : requests) {
			if (request.time() >= cutoff) {
				result.add(request);
			}
		}

		return result;
	}

	public void clear() {
		sessions.clear();
		authenticated.clear();
		requests.clear();
	}

	// ---------------------------------------------------------------- hashing

	private static String newSalt() {
		byte[] bytes = new byte[16];
		RANDOM.nextBytes(bytes);
		return toHex(bytes);
	}

	private static String hash(String salt, String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return toHex(digest.digest((salt + password).getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}

	private static String toHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder(bytes.length * 2);

		for (byte b : bytes) {
			builder.append(Character.forDigit((b >> 4) & 0xF, 16));
			builder.append(Character.forDigit(b & 0xF, 16));
		}

		return builder.toString();
	}

	private record Session(ServerLevel level, double x, double y, double z, float yaw, float pitch, int deadlineTick) {
	}
}
