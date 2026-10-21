package com.kaze943.oxycarbide.login;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;

/**
 * Blocks block interaction for players that have not logged in yet.
 *
 * <p>Movement and view are locked in {@link LoginManager#tick}, item usage is dropped in
 * {@code LoginItemMixin}, and this class covers right-clicking and breaking blocks.</p>
 */
public final class LoginProtection {
	private LoginProtection() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClientSide()) {
				return InteractionResult.PASS;
			}

			return LoginManager.get().isLocked(player) ? InteractionResult.FAIL : InteractionResult.PASS;
		});

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (world.isClientSide()) {
				return InteractionResult.PASS;
			}

			return LoginManager.get().isLocked(player) ? InteractionResult.FAIL : InteractionResult.PASS;
		});

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
				world.isClientSide() || !LoginManager.get().isLocked(player));
	}
}
