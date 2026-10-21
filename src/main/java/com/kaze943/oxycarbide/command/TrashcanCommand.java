package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.menu.TrashMenu;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;

/**
 * {@code /trashcan}: opens a temporary 27-slot container whose contents are discarded
 * when the screen is closed.
 */
public final class TrashcanCommand {
	private static final int SLOTS = 27;

	private TrashcanCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("trashcan")
						.executes(TrashcanCommand::open)));
	}

	private static int open(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!OxycarbideConfig.get().isTrashcanEnabled()) {
			source.sendFailure(Component.translatable("oxycarbide.trashcan.disabled"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		// The container only lives for the duration of the menu, so nothing is persisted.
		SimpleContainer trash = new SimpleContainer(SLOTS);

		player.openMenu(new SimpleMenuProvider(
				(containerId, playerInventory, opener) -> new TrashMenu(containerId, playerInventory, trash),
				Component.translatable("oxycarbide.trashcan.title")));

		return 1;
	}
}
