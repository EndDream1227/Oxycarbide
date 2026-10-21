package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.nickname.NicknameManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the vanilla display name with the player's chat nickname when one is set.
 *
 * <p>{@code Player#getDisplayName()} is what chat, the tab list and name tags use, so a
 * single injection covers all of them without touching the call sites.</p>
 */
@Mixin(Player.class)
public abstract class PlayerDisplayNameMixin {
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void oxycarbide$applyNickname(CallbackInfoReturnable<Component> callback) {
		Component nickname = NicknameManager.get().displayName((Player) (Object) this);

		if (nickname != null) {
			callback.setReturnValue(nickname);
		}
	}
}
