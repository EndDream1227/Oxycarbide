package com.kaze943.oxycarbide.claim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.util.Teleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Claim storage, access checks and the "grounded" push-back.
 *
 * <p>Claims live in {@code <world>/oxycarbide_claims.json}, keyed by
 * {@code dimension|chunkX|chunkZ}.</p>
 */
public final class ClaimManager {
	private static final ClaimManager INSTANCE = new ClaimManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Claim>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_claims.json";
	private static final int WARN_INTERVAL_TICKS = 20;

	private final Map<String, Claim> claims = new LinkedHashMap<>();
	private final Map<UUID, Position> lastPositions = new HashMap<>();
	private final Map<UUID, Integer> lastWarnTick = new HashMap<>();

	private Path file;

	private ClaimManager() {
	}

	public static ClaimManager get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		claims.clear();
		lastPositions.clear();
		lastWarnTick.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, Claim> loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				claims.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read claims from {}", file, e);
		}

		restore(server);
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(claims, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write claims to {}", file, e);
		}
	}

	public static String chunkKey(ServerLevel level, BlockPos pos) {
		return level.dimension().identifier() + "|" + (pos.getX() >> 4) + "|" + (pos.getZ() >> 4);
	}

	public Claim claimAt(ServerLevel level, BlockPos pos) {
		return claims.get(chunkKey(level, pos));
	}

	public Claim claimAt(Level level, BlockPos pos) {
		return level instanceof ServerLevel serverLevel ? claimAt(serverLevel, pos) : null;
	}

	public boolean claim(ServerLevel level, BlockPos pos, ServerPlayer player) {
		String key = chunkKey(level, pos);

		if (claims.containsKey(key)) {
			return false;
		}

		Claim claim = new Claim();
		claim.owner = player.getUUID().toString();
		claim.ownerName = player.getScoreboardName();
		claims.put(key, claim);
		save();
		return true;
	}

	public Claim remove(ServerLevel level, BlockPos pos) {
		Claim claim = claims.remove(chunkKey(level, pos));

		if (claim != null) {
			// Un-claiming a chunk also ends its forced loading.
			if (claim.forceLoaded) {
				level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
			}

			save();
		}

		return claim;
	}

	public void setForceLoaded(Claim claim, ServerLevel level, BlockPos pos, boolean loaded) {
		claim.forceLoaded = loaded;
		level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, loaded);
		save();
	}

	/** Re-applies the forced loading of stored claims after a server restart. */
	public void restore(MinecraftServer server) {
		for (Map.Entry<String, Claim> entry : claims.entrySet()) {
			Claim claim = entry.getValue();

			if (!claim.forceLoaded) {
				continue;
			}

			String[] parts = entry.getKey().split("\\|");

			if (parts.length != 3) {
				continue;
			}

			ServerLevel level = Teleporter.findLevel(server, parts[0]);

			if (level == null) {
				continue;
			}

			try {
				level.setChunkForced(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), true);
			} catch (NumberFormatException e) {
				Oxycarbide.LOGGER.warn("Skipping invalid claim key {}", entry.getKey());
			}
		}
	}

	public int countForceLoaded(UUID owner) {
		String id = owner.toString();
		int count = 0;

		for (Claim claim : claims.values()) {
			if (id.equals(claim.owner) && claim.forceLoaded) {
				count++;
			}
		}

		return count;
	}

	public int countOwnedBy(UUID owner) {
		String id = owner.toString();
		int count = 0;

		for (Claim claim : claims.values()) {
			if (id.equals(claim.owner)) {
				count++;
			}
		}

		return count;
	}

	public List<Claim> all() {
		return new ArrayList<>(claims.values());
	}

	public boolean isAllowed(Claim claim, ServerPlayer player) {
		if (isAdmin(player) || claim.publicOpen) {
			return true;
		}

		String id = player.getUUID().toString();

		if (claim.owner.equals(id)) {
			return true;
		}

		if (claim.whitelist != null && claim.whitelist.containsKey(id)) {
			return true;
		}

		return claim.openToTeam && sameTeam(player, claim.ownerName);
	}

	public boolean isAdmin(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		return server != null && server.getPlayerList().isOp(player.nameAndId());
	}

	private boolean sameTeam(ServerPlayer player, String ownerName) {
		Scoreboard scoreboard = player.level().getScoreboard();
		PlayerTeam ownTeam = player.getTeam();
		PlayerTeam ownerTeam = scoreboard.getPlayersTeam(ownerName);
		return ownTeam != null && ownTeam == ownerTeam;
	}

	/**
	 * @return {@code true} if the player may not interact with the block/entity at that
	 *         position (the player is told why)
	 */
	public boolean blocked(ServerPlayer player, ServerLevel level, BlockPos pos) {
		Claim claim = claimAt(level, pos);

		if (claim == null || isAllowed(claim, player)) {
			return false;
		}

		warn(player, ClaimTexts.component("protected", claim.ownerName));
		return true;
	}

	/** Pushes non-allowed players out of chunks that have "grounded" enabled. */
	public void tick(MinecraftServer server) {
		if (!OxycarbideConfig.get().isClaimEnabled()) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.isSpectator()) {
				continue;
			}

			ServerLevel level = (ServerLevel) player.level();
			Claim claim = claimAt(level, player.blockPosition());

			if (claim == null || !claim.grounded || isAllowed(claim, player)) {
				lastPositions.put(player.getUUID(), new Position(level,
						player.getX(), player.getY(), player.getZ()));
				continue;
			}

			Position last = lastPositions.get(player.getUUID());

			if (last == null || last.level() != level) {
				lastPositions.put(player.getUUID(), new Position(level,
						player.getX(), player.getY(), player.getZ()));
				continue;
			}

			player.connection.teleport(last.x(), last.y(), last.z(), player.getYRot(), player.getXRot());
			warn(player, ClaimTexts.component("grounded", claim.ownerName));
		}
	}

	private void warn(ServerPlayer player, Component message) {
		int now = player.tickCount;
		Integer last = lastWarnTick.get(player.getUUID());

		if (last != null && now - last < WARN_INTERVAL_TICKS) {
			return;
		}

		lastWarnTick.put(player.getUUID(), now);
		player.sendSystemMessage(message);
	}

	public void clear() {
		claims.clear();
		lastPositions.clear();
		lastWarnTick.clear();
	}

	private record Position(ServerLevel level, double x, double y, double z) {
	}
}
