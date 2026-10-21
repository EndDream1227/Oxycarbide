package com.kaze943.oxycarbide.prompt;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends a prompt either to the chat window ({@code dialog}) or to the subtitle area
 * ({@code title}), depending on the player's {@link PromptModes} choice.
 */
public final class PromptDisplay {
	private static final int FADE_IN = 10;
	private static final int STAY = 60;
	private static final int FADE_OUT = 20;

	private PromptDisplay() {
	}

	/**
	 * Shows the message in the subtitle area.
	 *
	 * @return {@code false} when the player has no connection, so the caller can fall
	 *         back to the usual chat message
	 */
	public static boolean showTitle(ServerPlayer player, Component message) {
		if (player.connection == null) {
			return false;
		}

		player.connection.send(new ClientboundSetTitlesAnimationPacket(FADE_IN, STAY, FADE_OUT));
		// An empty title makes the client render the subtitle without covering the screen.
		player.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
		player.connection.send(new ClientboundSetSubtitleTextPacket(message));
		return true;
	}

	/** Sends one prompt to a single player, honouring their chosen mode. */
	public static void send(ServerPlayer player, Component message) {
		if (PromptModes.get().isTitle(player.getUUID()) && showTitle(player, message)) {
			return;
		}

		player.sendSystemMessage(message);
	}
}
