package com.kaze943.oxycarbide.sweep;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.prompt.PromptDisplay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Periodic and smart sweeps of dropped items (and monsters in {@code entity} mode).
 */
public final class SweepManager {
	private static final SweepManager INSTANCE = new SweepManager();

	private long lastTickNanos;
	private double smoothedMs;
	private int ticks;
	private int autoSeconds;
	private int secondsSinceScan;
	private int secondsSinceSmartSweep;
	private int lastDropCount;

	private SweepManager() {
	}

	public static SweepManager get() {
		return INSTANCE;
	}

	public void tick(MinecraftServer server) {
		sampleTickTime();
		ticks++;

		if (ticks % 20 != 0) {
			return;
		}

		OxycarbideConfig config = OxycarbideConfig.get();

		if (!config.isSweepEnabled()) {
			resetTimers();
			return;
		}

		autoTick(server, config);
		smartTick(server, config);
	}

	private void resetTimers() {
		autoSeconds = 0;
		secondsSinceScan = 0;
		secondsSinceSmartSweep = 0;
		lastDropCount = 0;
	}

	private void sampleTickTime() {
		long now = System.nanoTime();

		if (lastTickNanos != 0) {
			double ms = (now - lastTickNanos) / 1_000_000.0;
			smoothedMs = smoothedMs == 0 ? ms : smoothedMs * 0.9 + ms * 0.1;
		}

		lastTickNanos = now;
	}

	private void autoTick(MinecraftServer server, OxycarbideConfig config) {
		int interval = config.getSweepTimeMinutes() * 60;

		if (interval <= 0) {
			autoSeconds = 0;
			return;
		}

		autoSeconds++;
		int remaining = interval - autoSeconds;

		if (remaining <= 0) {
			autoSeconds = 0;
			SweepResult result = sweep(server, config);
			announce(server, config, result);
			return;
		}

		if (!config.isSilentSweep() && parseCountdown(config.getSweepCountdownSeconds()).contains(remaining)) {
			broadcast(server, SweepTexts.text("countdown", remaining));
		}
	}

	private void smartTick(MinecraftServer server, OxycarbideConfig config) {
		if (!config.isSmartSweep()) {
			return;
		}

		secondsSinceScan++;

		if (secondsSinceScan >= config.getSmartScanCooldownSeconds()) {
			secondsSinceScan = 0;
			lastDropCount = countDrops(server);
		}

		secondsSinceSmartSweep++;

		double tps = tps();

		if (lastDropCount < config.getSmartDropThreshold()
				|| tps >= config.getSmartTpsThreshold()
				|| secondsSinceSmartSweep < config.getSmartSweepCooldownSeconds()) {
			return;
		}

		secondsSinceSmartSweep = 0;
		secondsSinceScan = 0;
		lastDropCount = 0;
		SweepResult result = sweep(server, config);

		if (!config.isSilentSweep()) {
			broadcast(server, SweepTexts.text("smart", result.items()));
		}
	}

	/** Triggered by {@code /sweep}: resets the automatic timers. */
	public SweepResult sweepNow(MinecraftServer server) {
		resetTimers();
		return sweep(server, OxycarbideConfig.get());
	}

	public SweepResult sweep(MinecraftServer server, OxycarbideConfig config) {
		boolean entityMode = "entity".equalsIgnoreCase(config.getSweepMode());
		int items = 0;
		int mobs = 0;

		for (ServerLevel level : server.getAllLevels()) {
			List<Entity> snapshot = new ArrayList<>();
			level.getAllEntities().forEach(snapshot::add);

			for (Entity entity : snapshot) {
				if (entity instanceof ItemEntity itemEntity) {
					String id = BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem()).toString();

					if (config.getSweepWhitelist().contains(id)) {
						continue;
					}

					itemEntity.discard();
					items++;
				} else if (entityMode && entity instanceof Monster monster) {
					String id = BuiltInRegistries.ENTITY_TYPE.getKey(monster.getType()).toString();

					if (config.getSweepWhitelist().contains(id)) {
						continue;
					}

					monster.discard();
					mobs++;
				}
			}
		}

		return new SweepResult(items, mobs);
	}

	public void announce(MinecraftServer server, OxycarbideConfig config, SweepResult result) {
		if (config.isSilentSweep()) {
			return;
		}

		String text = "entity".equalsIgnoreCase(config.getSweepMode())
				? SweepTexts.text("clear_entity", result.items(), result.mobs())
				: SweepTexts.text("clear", result.items());
		broadcast(server, text);
	}

	private void broadcast(MinecraftServer server, String text) {
		Component message = Component.literal(text);

		// Send per player so everybody's /oxycarbide config promptmode choice is honoured.
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PromptDisplay.send(player, message);
		}
	}

	private static int countDrops(MinecraftServer server) {
		int count = 0;

		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				if (entity instanceof ItemEntity) {
					count++;
				}
			}
		}

		return count;
	}

	private static Set<Integer> parseCountdown(String value) {
		Set<Integer> result = new HashSet<>();

		if (value == null || value.isBlank()) {
			return result;
		}

		for (String part : value.split("[,，\\s]+")) {
			try {
				result.add(Integer.parseInt(part.trim()));
			} catch (NumberFormatException ignored) {
				// Skip malformed entries.
			}
		}

		return result;
	}

	/** Smoothed server TPS, capped at 20. */
	public double tps() {
		return smoothedMs <= 0 ? 20.0 : Math.min(20.0, 1000.0 / smoothedMs);
	}

	public record SweepResult(int items, int mobs) {
	}
}
