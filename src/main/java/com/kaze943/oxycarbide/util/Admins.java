package com.kaze943.oxycarbide.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Operator checks shared by every command of the mod.
 */
public final class Admins {
	private Admins() {
	}

	/**
	 * Operators may use the administrative commands and are never bound by the home or
	 * warp limits. Non-player sources (console, command blocks) count as operators.
	 */
	public static boolean isAdmin(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			return true;
		}

		return source.getServer().getPlayerList().isOp(player.nameAndId());
	}
}
