package com.kaze943.oxycarbide.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * A plain 3-row chest menu backed by a temporary container. When the menu is closed the
 * container is cleared, so anything the player put in is gone.
 */
public final class TrashMenu extends ChestMenu {
	private final Container trash;

	public TrashMenu(int containerId, Inventory playerInventory, Container trash) {
		super(MenuType.GENERIC_9x3, containerId, playerInventory, trash, 3);
		this.trash = trash;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		trash.clearContent();
	}
}
