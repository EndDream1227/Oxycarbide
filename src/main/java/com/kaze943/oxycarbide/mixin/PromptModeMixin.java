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
 * Reroutes chat messages to the subtitle area for players that picked
 * {@code /oxycarbide config promptmode title}.
 */
@Mixin(ServerPlayer.class)
public abstract class PromptModeMixin {
	@Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
			at = @At("HEAD"), cancellable = true)
	private void oxycarbide$promptAsTitle(Component message, CallbackInfo callback) {
		if (message == null || message.getString().isBlank()) {
			return;
		}

		ServerPlayer player = (ServerPlayer) (Object) this;

		if (!PromptModes.get().isTitle(player.getUUID())) {
			return;
		}

		if (PromptDisplay.showTitle(player, message)) {
			callback.cancel();
		}
	}
}
