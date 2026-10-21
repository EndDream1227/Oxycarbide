package com.kaze943.oxycarbide.market;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Global market menu: display-only slots, left clicks are forwarded to
 * {@link MarketService}.
 */
public final class MarketMenu extends ChestMenu {
	private final Container display;
	private final int page;

	public MarketMenu(int containerId, Inventory playerInventory, Container display,
			int rows, MenuType<?> type, int page) {
		super(type, containerId, playerInventory, display, rows);
		this.display = display;
		this.page = page;
	}

	@Override
	public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
		if (slotIndex >= 0 && slotIndex < display.getContainerSize()) {
			if (containerInput == ContainerInput.PICKUP && buttonNum == 0 && player instanceof ServerPlayer serverPlayer) {
				MarketService.get().click(serverPlayer, page, slotIndex);
			}

			return;
		}

		super.clicked(slotIndex, buttonNum, containerInput, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
