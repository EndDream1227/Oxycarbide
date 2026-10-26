package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.nickname.NicknameManager;
import com.kaze943.oxycarbide.title.TitlesManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the worn title (and the nickname, when one is set) in front of the player name.
 *
 * <p>{@code Player#getDisplayName()} is what chat, the tab list and the server side uses,
 * so a single injection covers them all without patching every call site. The name tag
 * above the head is the entity's custom name, set by {@link TitlesManager}.</p>
 */
@Mixin(Player.class)
public abstract class PlayerDisplayNameMixin {
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void oxycarbide$applyDisplayName(CallbackInfoReturnable<Component> callback) {
		Player player = (Player) (Object) this;
		Component nickname = NicknameManager.get().displayName(player);
		Component decorated = TitlesManager.get().decorate(player, nickname);

		if (decorated != null) {
			callback.setReturnValue(decorated);
		} else if (nickname != null) {
			callback.setReturnValue(nickname);
		}
	}
}
