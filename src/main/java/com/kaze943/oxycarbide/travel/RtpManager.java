package com.kaze943.oxycarbide.travel;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.util.Teleporter;
import com.kaze943.oxycarbide.util.TextComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs {@code /rtp} without blocking the server thread.
 *
 * <p>Every server tick each request generates at most one candidate chunk and looks for a
 * safe column in it. When a spot is found the request moves into a countdown state and
 * only teleports once the countdown (configured in ticks) has passed.</p>
 */
public final class RtpManager {
	private static final RtpManager INSTANCE = new RtpManager();

	/** Upper bound of chunks a single {@code /rtp} may generate. */
	private static final int MAX_ATTEMPTS = 32;

	private final Map<UUID, RtpRequest> requests = new LinkedHashMap<>();
	private final Map<UUID, Integer> lastTeleportTick = new HashMap<>();
	private int requestsThisTick;

	private RtpManager() {
	}

	public static RtpManager get() {
		return INSTANCE;
	}

	/**
	 * Validates a new {@code /rtp} request and queues it.
	 *
	 * @return {@code true} if the request was accepted
	 */
	public boolean request(CommandSourceStack source, ServerPlayer player) {
		MinecraftServer server = source.getServer();
		OxycarbideConfig config = OxycarbideConfig.get();
		UUID uuid = player.getUUID();

		int remaining = cooldownRemaining(server, uuid);

		if (remaining > 0) {
			source.sendFailure(RtpTexts.component("cooldown", seconds(remaining)));
			return false;
		}

		if (requests.containsKey(uuid)) {
			source.sendFailure(RtpTexts.component("busy"));
			return false;
		}

		if (config.isRtpAutoProtect()) {
			requestsThisTick++;

			if (requestsThisTick >= config.getRtpAutoProtectNum()) {
				// Too many players asked at the same time: drop everything, including
				// the requests that arrived earlier in this tick.
				cancelAll(server);
				source.sendFailure(RtpTexts.component("busy"));
				return false;
			}

			if (!requests.isEmpty()) {
				// Another player is already searching or counting down; keep the first
				// request and reject this one.
				source.sendFailure(RtpTexts.component("busy"));
				return false;
			}
		}

		int minRadius = config.getRtpRadius();
		int maxRadius = Math.max(minRadius * 2, minRadius + 1000);

		requests.put(uuid, new RtpRequest(uuid, source.getLevel(), player.getX(), player.getZ(),
				minRadius, maxRadius, MAX_ATTEMPTS));
		source.sendSuccess(() -> RtpTexts.component("searching"), false);
		return true;
	}

	/** Called once per server tick. */
	public void tick(MinecraftServer server) {
		requestsThisTick = 0;

		if (requests.isEmpty()) {
			return;
		}

		Iterator<RtpRequest> iterator = requests.values().iterator();

		while (iterator.hasNext()) {
			RtpRequest request = iterator.next();
			ServerPlayer player = server.getPlayerList().getPlayer(request.player());

			if (player == null) {
				iterator.remove();
				continue;
			}

			if (request.state() == RtpRequest.State.SEARCHING) {
				if (searchOneChunk(request, player)) {
					continue;
				}

				request.consumeAttempt();

				if (request.attemptsLeft() <= 0) {
					player.sendSystemMessage(RtpTexts.component("failed"));
					iterator.remove();
				}
			} else {
				request.tickCountdown();

				if (request.ticksLeft() <= 0) {
					teleport(server, request, player);
					iterator.remove();
				}
			}
		}
	}

	private boolean searchOneChunk(RtpRequest request, ServerPlayer player) {
		ServerLevel level = request.level();
		ChunkPos chunk = RandomTeleport.pickChunk(level, request.originX(), request.originZ(),
				request.minRadius(), request.maxRadius(), level.getRandom());

		if (chunk == null) {
			return false;
		}

		BlockPos spot = RandomTeleport.findSafeSpot(level, chunk, level.getRandom());

		if (spot == null) {
			return false;
		}

		request.setTarget(spot);
		request.setState(RtpRequest.State.COUNTDOWN);
		request.setTicksLeft(Math.max(0, OxycarbideConfig.get().getRtpCountdown()));

		player.sendSystemMessage(RtpTexts.component("countdown", seconds(request.ticksLeft())));
		return true;
	}

	private void teleport(MinecraftServer server, RtpRequest request, ServerPlayer player) {
		BlockPos target = request.target();

		if (target == null) {
			player.sendSystemMessage(RtpTexts.component("failed"));
			return;
		}

		ServerLevel level = Teleporter.findLevel(server, request.level().dimension().identifier().toString());

		if (level == null) {
			player.sendSystemMessage(RtpTexts.component("failed"));
			return;
		}

		// The countdown may have outlived the chunk ticket, so make sure it is loaded.
		level.getChunk(target.getX() >> 4, target.getZ() >> 4);

		Teleporter.teleport(player, level, target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
				player.getYRot(), player.getXRot());

		lastTeleportTick.put(player.getUUID(), server.getTickCount());

		Component coordinates = TextComponents.clickableCoordinates(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
		player.sendSystemMessage(RtpTexts.component("teleport", coordinates));
	}

	private int cooldownRemaining(MinecraftServer server, UUID player) {
		Integer last = lastTeleportTick.get(player);

		if (last == null) {
			return 0;
		}

		int elapsed = server.getTickCount() - last;
		return Math.max(0, OxycarbideConfig.get().getRtpCooldown() - elapsed);
	}

	private void cancelAll(MinecraftServer server) {
		List<RtpRequest> pending = new ArrayList<>(requests.values());
		requests.clear();

		for (RtpRequest request : pending) {
			ServerPlayer player = server.getPlayerList().getPlayer(request.player());

			if (player != null) {
				player.sendSystemMessage(RtpTexts.component("cancelled"));
			}
		}
	}

	/** Ticks are counted in game ticks, the messages talk in seconds. */
	private static int seconds(int ticks) {
		return Math.max(1, (ticks + 19) / 20);
	}

	public void clear() {
		requests.clear();
		lastTeleportTick.clear();
		requestsThisTick = 0;
	}
}
