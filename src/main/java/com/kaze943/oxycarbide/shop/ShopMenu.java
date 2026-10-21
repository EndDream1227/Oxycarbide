package com.kaze943.oxycarbide.shop;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * A chest menu whose slots are display-only: left clicks trigger the shop trade instead
 * of moving items.
 */
public final class ShopMenu extends ChestMenu {
	private final ShopDefinition shop;
	private final Container display;

	public ShopMenu(int containerId, Inventory playerInventory, ShopDefinition shop,
			Container display, int rows, MenuType<?> type) {
		super(type, containerId, playerInventory, display, rows);
		this.shop = shop;
		this.display = display;
	}

	@Override
	public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
		if (slotIndex >= 0 && slotIndex < display.getContainerSize()) {
			if (containerInput == ContainerInput.PICKUP && buttonNum == 0 && player instanceof ServerPlayer serverPlayer) {
				ShopService.get().handleClick(serverPlayer, shop, slotIndex);
			}

			// Never move items out of a shop slot.
			return;
		}

		super.clicked(slotIndex, buttonNum, containerInput, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
