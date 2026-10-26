package com.kaze943.oxycarbide.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * A chest menu whose slots are display-only: left clicks trigger the button action
 * instead of moving items.
 */
public final class MenuMenu extends ChestMenu {
	private final MenuDefinition menu;
	private final Container display;

	public MenuMenu(int containerId, Inventory playerInventory, MenuDefinition menu,
			Container display, int rows, MenuType<?> type) {
		super(type, containerId, playerInventory, display, rows);
		this.menu = menu;
		this.display = display;
	}

	@Override
	public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
		if (slotIndex >= 0 && slotIndex < display.getContainerSize()) {
			if (containerInput == ContainerInput.PICKUP && buttonNum == 0 && player instanceof ServerPlayer serverPlayer) {
				MenuService.get().handleClick(serverPlayer, menu, slotIndex);
			}

			// Never move items out of a menu slot.
			return;
		}

		super.clicked(slotIndex, buttonNum, containerInput, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
