package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.hat.HatTexts;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * {@code /hat}: moves the item in the main hand onto the player's head.
 *
 * <p>Only a single item is moved, so a stack of e.g. 64 blocks turns into one block on
 * the head and 63 left in the hand. Whatever was worn before is put back into the hand,
 * or into the inventory / dropped when the hand is still occupied by the rest of the
 * stack.</p>
 */
public final class HatCommands {
	private HatCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("hat")
						.executes(HatCommands::hat)));
	}

	private static int hat(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.hat.player_only"));
			return 0;
		}

		ItemStack held = player.getMainHandItem();

		if (held.isEmpty()) {
			source.sendFailure(HatTexts.component("empty"));
			return 0;
		}

		ItemStack previous = player.getItemBySlot(EquipmentSlot.HEAD);

		// Exactly one item goes to the head; the rest of the stack stays in the hand.
		ItemStack hat = held.copyWithCount(1);
		held.shrink(1);

		if (held.isEmpty()) {
			player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}

		player.setItemSlot(EquipmentSlot.HEAD, hat);

		if (!previous.isEmpty()) {
			if (player.getMainHandItem().isEmpty()) {
				// The hand is free again, so swap like vanilla armour does.
				player.setItemSlot(EquipmentSlot.MAINHAND, previous);
			} else if (!player.getInventory().add(previous)) {
				// Inventory is full: drop it at the player's feet. The drop is authoritative
				// on the server, so PREDICTED (client-side prediction) must not be used.
				player.drop(previous, false, Prediction.SERVER_ONLY);
			}
		}

		source.sendSuccess(() -> HatTexts.component("success", hat.getHoverName()), false);
		return 1;
	}
}
