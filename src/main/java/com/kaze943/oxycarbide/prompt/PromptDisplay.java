package com.kaze943.oxycarbide.prompt;

import com.kaze943.oxycarbide.text.Translations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends a prompt either to the chat window ({@code dialog}) or to the action bar
 * ({@code actionbar}), the small text that sits just above the hotbar.
 */
public final class PromptDisplay {
	/**
	 * Set while {@link #sendToChat} delivers a message, so that the action bar re-routing
	 * in {@code PromptModeMixin} leaves it alone.
	 */
	private static final ThreadLocal<Boolean> FORCE_CHAT = ThreadLocal.withInitial(() -> Boolean.FALSE);

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

		player.connection.send(new ClientboundSetActionBarTextPacket(localize(player, message)));
		return true;
	}

	/** Sends one prompt to a single player, honouring their chosen mode. */
	public static void send(ServerPlayer player, Component message) {
		Component localized = localize(player, message);

		if (PromptModes.get().isActionBar(player.getUUID()) && showActionBar(player, localized)) {
			return;
		}

		player.sendSystemMessage(localized);
	}

	/**
	 * Sends a message to the chat window even when the player shows prompts on the action
	 * bar. Requests whose buttons have to stay clickable (such as {@code /tpa}) use this,
	 * because the action bar cannot be clicked.
	 */
	public static void sendToChat(ServerPlayer player, Component message) {
		boolean previous = FORCE_CHAT.get();
		FORCE_CHAT.set(Boolean.TRUE);

		try {
			player.sendSystemMessage(localize(player, message));
		} finally {
			FORCE_CHAT.set(previous);
		}
	}

	/** {@code true} while {@link #sendToChat} is delivering a message. */
	public static boolean forcedChat() {
		return FORCE_CHAT.get();
	}

	/** Resolves the text in the player's language; already resolved texts pass through. */
	private static Component localize(ServerPlayer player, Component message) {
		return Translations.resolveFor(player, message);
	}
}
