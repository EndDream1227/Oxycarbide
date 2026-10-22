package com.kaze943.oxycarbide.prompt;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends a prompt either to the chat window ({@code dialog}) or to the action bar
 * ({@code actionbar}), the small text that sits just above the hotbar.
 */
public final class PromptDisplay {
	private PromptDisplay() {
	}

	/**
	 * Shows the message on the action bar, right above the hotbar. The client renders it
	 * in the normal (small) chat font and fades it out on its own.
	 *
	 * @return {@code false} when the player has no connection, so the caller can fall
	 *         back to the usual chat message
	 */
	public static boolean showActionBar(ServerPlayer player, Component message) {
		if (player.connection == null) {
			return false;
		}

		player.connection.send(new ClientboundSetActionBarTextPacket(message));
		return true;
	}

	/** Sends one prompt to a single player, honouring their chosen mode. */
	public static void send(ServerPlayer player, Component message) {
		if (PromptModes.get().isActionBar(player.getUUID()) && showActionBar(player, message)) {
			return;
		}

		player.sendSystemMessage(message);
	}
}
