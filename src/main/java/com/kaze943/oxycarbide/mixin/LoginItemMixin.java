package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.login.LoginManager;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops "use item" packets from players that have not logged in yet.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class LoginItemMixin {
	@Inject(method = "handleUseItem", at = @At("HEAD"), cancellable = true)
	private void oxycarbide$blockUseItem(ServerboundUseItemPacket packet, CallbackInfo callback) {
		ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
		ServerPlayer player = self.getPlayer();

		if (player != null && LoginManager.get().isLocked(player)) {
			callback.cancel();
		}
	}
}
