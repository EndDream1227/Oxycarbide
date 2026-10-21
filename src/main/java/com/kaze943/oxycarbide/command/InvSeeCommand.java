package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.inventory.PlayerInventoryContainer;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * {@code /invsee <player>}: opens a 4-row chest menu showing the target's real inventory.
 */
public final class InvSeeCommand {
	private InvSeeCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("invsee")
						.requires(Admins::isAdmin)
						.then(Commands.argument("player", StringArgumentType.word())
								.executes(InvSeeCommand::open))));
	}

	private static int open(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer viewer = source.getPlayer();

		if (viewer == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String targetName = StringArgumentType.getString(context, "player");
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);

		if (target == null) {
			source.sendFailure(Component.translatable("oxycarbide.invsee.target_not_found", targetName));
			return 0;
		}

		PlayerInventoryContainer container = new PlayerInventoryContainer(target.getInventory());

		viewer.openMenu(new SimpleMenuProvider(
				(containerId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x4,
						containerId, playerInventory, container, 4),
				Component.translatable("oxycarbide.invsee.title", target.getScoreboardName())));

		return 1;
	}
}
