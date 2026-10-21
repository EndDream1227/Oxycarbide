package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.mute.MuteManager;
import com.kaze943.oxycarbide.mute.MuteTexts;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops chat messages from muted players before they are processed or broadcast.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMuteMixin {
	@Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
	private void oxycarbide$blockMutedChat(ServerboundChatPacket packet, CallbackInfo callback) {
		if (!OxycarbideConfig.get().isMuteEnabled()) {
			return;
		}

		ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
		ServerPlayer player = self.getPlayer();

		if (player == null) {
			return;
		}

		MuteManager manager = MuteManager.get();

		if (!manager.isMuted(player.getUUID())) {
			return;
		}

		long remaining = manager.remainingSeconds(player.getUUID());

		if (remaining < 0) {
			player.sendSystemMessage(MuteTexts.component("blocked_permanent"));
		} else {
			player.sendSystemMessage(MuteTexts.component("blocked", remaining));
		}

		callback.cancel();
	}
}
