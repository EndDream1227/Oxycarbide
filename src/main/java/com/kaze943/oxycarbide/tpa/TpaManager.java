package com.kaze943.oxycarbide.tpa;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.prompt.PromptDisplay;
import com.kaze943.oxycarbide.prompt.PromptModes;
import com.kaze943.oxycarbide.util.Teleporter;
import com.kaze943.oxycarbide.util.TextComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles {@code /tpa} and {@code /tpahere}: a request waits for the target to click
 * accept or deny, expires on its own after the configured auto-deny time, and the
 * accepted teleport is delayed by the configured countdown.
 */
public final class TpaManager {
	public enum Type {
		TPA,
		TPAHERE
	}

	private static final TpaManager INSTANCE = new TpaManager();

	/** How many open requests a single player may have, to avoid chat spam. */
	private static final int MAX_PENDING_PER_TARGET = 10;

	/** target -> open requests, oldest first. */
	private final Map<UUID, List<Request>> requests = new HashMap<>();
	/** player that will be moved -> where they will go. */
	private final Map<UUID, PendingTeleport> teleports = new HashMap<>();
	private final Map<UUID, Integer> lastRequestTick = new HashMap<>();

	private TpaManager() {
	}

	public static TpaManager get() {
		return INSTANCE;
	}

	public boolean request(MinecraftServer server, CommandSourceStack source, ServerPlayer requester,
			ServerPlayer target, Type type) {
		int remaining = cooldownRemaining(server, requester.getUUID());

		if (remaining > 0) {
			source.sendFailure(TpaTexts.component("cooldown", seconds(remaining)));
			return false;
		}

		List<Request> pending = requests.computeIfAbsent(target.getUUID(), key -> new ArrayList<>());

		if (pending.size() >= MAX_PENDING_PER_TARGET) {
			source.sendFailure(TpaTexts.component("target_busy"));
			return false;
		}

		int now = server.getTickCount();
		pending.add(new Request(requester.getUUID(), now, type));
		lastRequestTick.put(requester.getUUID(), now);

		requester.sendSystemMessage(TpaTexts.component("request_sent", target.getScoreboardName()));
		notifyTarget(target, requester.getScoreboardName(), type);
		return true;
	}

	public boolean accept(MinecraftServer server, ServerPlayer acceptor) {
		Request request = pollOldest(acceptor.getUUID());

		if (request == null) {
			acceptor.sendSystemMessage(Component.translatable("oxycarbide.tpa.none"));
			return false;
		}

		ServerPlayer requester = server.getPlayerList().getPlayer(request.requester());

		if (requester == null) {
			acceptor.sendSystemMessage(TpaTexts.component("target_offline"));
			return false;
		}

		acceptor.sendSystemMessage(TpaTexts.component("accepted_self"));
		requester.sendSystemMessage(TpaTexts.component("accepted")
				.copy().withStyle(style -> style.withColor(ChatFormatting.GREEN)));

		int countdown = Math.max(0, OxycarbideConfig.get().getTpaCountdown());
		UUID traveller = request.type() == Type.TPA ? requester.getUUID() : acceptor.getUUID();
		UUID destination = request.type() == Type.TPA ? acceptor.getUUID() : requester.getUUID();

		teleports.put(traveller, new PendingTeleport(destination, countdown));

		Component countdownMessage = TpaTexts.component("countdown", seconds(countdown));
		requester.sendSystemMessage(countdownMessage);

		if (!traveller.equals(requester.getUUID())) {
			ServerPlayer travellerPlayer = server.getPlayerList().getPlayer(traveller);

			if (travellerPlayer != null) {
				travellerPlayer.sendSystemMessage(countdownMessage);
			}
		}

		return true;
	}

	public boolean deny(MinecraftServer server, ServerPlayer denier) {
		Request request = pollOldest(denier.getUUID());

		if (request == null) {
			denier.sendSystemMessage(Component.translatable("oxycarbide.tpa.none"));
			return false;
		}

		ServerPlayer requester = server.getPlayerList().getPlayer(request.requester());

		if (requester != null) {
			requester.sendSystemMessage(TpaTexts.component("denied")
					.copy().withStyle(style -> style.withColor(ChatFormatting.RED)));
		}

		denier.sendSystemMessage(TpaTexts.component("denied_self"));
		return true;
	}

	/** Called once per server tick. */
	public void tick(MinecraftServer server) {
		expireRequests(server);

		if (teleports.isEmpty()) {
			return;
		}

		Iterator<Map.Entry<UUID, PendingTeleport>> iterator = teleports.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, PendingTeleport> entry = iterator.next();
			PendingTeleport pending = entry.getValue();
			pending.ticksLeft--;

			if (pending.ticksLeft > 0) {
				continue;
			}

			iterator.remove();
			executeTeleport(server, entry.getKey(), pending.destination);
		}
	}

	private void executeTeleport(MinecraftServer server, UUID travellerId, UUID destinationId) {
		ServerPlayer traveller = server.getPlayerList().getPlayer(travellerId);

		if (traveller == null) {
			return;
		}

		ServerPlayer destination = server.getPlayerList().getPlayer(destinationId);

		if (destination == null) {
			traveller.sendSystemMessage(TpaTexts.component("target_offline"));
			return;
		}

		ServerLevel level = (ServerLevel) destination.level();

		Teleporter.teleport(traveller, level, destination.getX(), destination.getY(), destination.getZ(),
				destination.getYRot(), destination.getXRot());
		traveller.sendSystemMessage(TpaTexts.component("teleported", destination.getScoreboardName()));
	}

	private void expireRequests(MinecraftServer server) {
		int now = server.getTickCount();
		int timeout = OxycarbideConfig.get().getTpaAutoDeny();
		Iterator<Map.Entry<UUID, List<Request>>> iterator = requests.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, List<Request>> entry = iterator.next();
			Iterator<Request> pending = entry.getValue().iterator();

			while (pending.hasNext()) {
				Request request = pending.next();

				if (now - request.createdTick() < timeout) {
					continue;
				}

				pending.remove();
				ServerPlayer requester = server.getPlayerList().getPlayer(request.requester());

				if (requester != null) {
					requester.sendSystemMessage(TpaTexts.component("timeout"));
				}
			}

			if (entry.getValue().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private Request pollOldest(UUID target) {
		List<Request> pending = requests.get(target);

		if (pending == null || pending.isEmpty()) {
			return null;
		}

		Request request = pending.remove(0);

		if (pending.isEmpty()) {
			requests.remove(target);
		}

		return request;
	}

	/**
	 * The accept/deny buttons are clickable text, so a request always goes to the chat:
	 * the action bar cannot be clicked. Everything else follows the player's prompt mode.
	 */
	private void notifyTarget(ServerPlayer target, String requesterName, Type type) {
		Component message = incomingMessage(requesterName, type);

		if (PromptModes.get().isActionBar(target.getUUID())) {
			PromptDisplay.sendToChat(target, message);
			return;
		}

		target.sendSystemMessage(message);
	}

	private Component incomingMessage(String requesterName, Type type) {
		Component acceptButton = TextComponents.clickableCommand("oxycarbide.tpa.accept", "/tpaccept",
				ChatFormatting.GREEN, "oxycarbide.tpa.accept.hover");
		Component denyButton = TextComponents.clickableCommand("oxycarbide.tpa.deny", "/tpadeny",
				ChatFormatting.RED, "oxycarbide.tpa.deny.hover");

		Component firstLine = TpaTexts.component(
				type == Type.TPA ? "incoming" : "incoming_here",
				requesterName, acceptButton, denyButton);
		Component secondLine = TpaTexts.component("auto_deny",
				seconds(OxycarbideConfig.get().getTpaAutoDeny()));

		return Component.empty().append(firstLine).append(Component.literal("\n")).append(secondLine);
	}

	private int cooldownRemaining(MinecraftServer server, UUID player) {
		Integer last = lastRequestTick.get(player);

		if (last == null) {
			return 0;
		}

		return Math.max(0, OxycarbideConfig.get().getTpaCooldown() - (server.getTickCount() - last));
	}

	/** Ticks rounded up to whole seconds, never below one. */
	private static int seconds(int ticks) {
		return Math.max(1, (ticks + 19) / 20);
	}

	public void clear() {
		requests.clear();
		teleports.clear();
		lastRequestTick.clear();
	}

	private record Request(UUID requester, int createdTick, Type type) {
	}

	private static final class PendingTeleport {
		private final UUID destination;
		private int ticksLeft;

		private PendingTeleport(UUID destination, int ticksLeft) {
			this.destination = destination;
			this.ticksLeft = ticksLeft;
		}
	}
}
