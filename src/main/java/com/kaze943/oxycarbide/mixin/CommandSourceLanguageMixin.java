package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.text.Translations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

/**
 * Keeps console and command-block feedback readable: a source without a player gets the
 * text in the server's own language instead of the internal placeholder key.
 *
 * <p>Messages for a player are left alone here so that
 * {@link LanguageMixin} can pick the language of that player.</p>
 */
@Mixin(CommandSourceStack.class)
public abstract class CommandSourceLanguageMixin {
	@ModifyVariable(method = "sendFailure(Lnet/minecraft/network/chat/Component;)V",
			at = @At("HEAD"), argsOnly = true)
	private Component oxycarbide$localizeFailure(Component message) {
		CommandSourceStack source = (CommandSourceStack) (Object) this;
		return source.getPlayer() == null ? Translations.resolveDefault(message) : message;
	}

	@ModifyVariable(method = "sendSuccess(Ljava/util/function/Supplier;Z)V",
			at = @At("HEAD"), argsOnly = true)
	private Supplier<Component> oxycarbide$localizeSuccess(Supplier<Component> message) {
		CommandSourceStack source = (CommandSourceStack) (Object) this;

		if (source.getPlayer() != null) {
			return message;
		}

		return () -> Translations.resolveDefault(message.get());
	}
}
