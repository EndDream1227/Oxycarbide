package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.prompt.PromptDisplay;
import com.kaze943.oxycarbide.prompt.PromptModes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reroutes chat messages to the action bar (the small text above the hotbar) for players
 * that picked {@code /oxycarbide config promptmode actionbar}.
 */
@Mixin(ServerPlayer.class)
public abstract class PromptModeMixin {
	@Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
			at = @At("HEAD"), cancellable = true)
	private void oxycarbide$promptAsActionBar(Component message, CallbackInfo callback) {
		if (message == null || message.getString().isBlank()) {
			return;
		}

		ServerPlayer player = (ServerPlayer) (Object) this;

		if (!PromptModes.get().isActionBar(player.getUUID())) {
			return;
		}

		if (PromptDisplay.showActionBar(player, message)) {
			callback.cancel();
		}
	}
}
