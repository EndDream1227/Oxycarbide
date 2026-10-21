package com.kaze943.oxycarbide.claim;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/**
 * Blocks interaction with blocks (including block entities) and entities inside chunks
 * that belong to somebody else.
 */
public final class ClaimProtection {
	private ClaimProtection() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel level)) {
				return InteractionResult.PASS;
			}

			return ClaimManager.get().blocked(serverPlayer, level, hitResult.getBlockPos())
					? InteractionResult.FAIL
					: InteractionResult.PASS;
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel level)) {
				return InteractionResult.PASS;
			}

			return ClaimManager.get().blocked(serverPlayer, level, entity.blockPosition())
					? InteractionResult.FAIL
					: InteractionResult.PASS;
		});
	}
}
