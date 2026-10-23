package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.text.Translations;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Turns the placeholders built by {@code config/oxycarbide/lang} into the text of the
 * language the receiving client selected.
 *
 * <p>This has to happen here, right before the message leaves the server, because the
 * same message object may be built for a Chinese and an English player in the same
 * tick.</p>
 */
@Mixin(ServerPlayer.class)
public abstract class LanguageMixin {
	@ModifyVariable(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
			at = @At("HEAD"), argsOnly = true)
	private Component oxycarbide$localize(Component message) {
		return Translations.resolveFor((ServerPlayer) (Object) this, message);
	}

	@ModifyVariable(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
			at = @At("HEAD"), argsOnly = true)
	private Component oxycarbide$localizeOverlay(Component message) {
		return Translations.resolveFor((ServerPlayer) (Object) this, message);
	}
}
