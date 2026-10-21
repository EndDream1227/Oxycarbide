package com.kaze943.oxycarbide.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A live, 36-slot view over a player's main inventory (hotbar + backpack), used by
 * {@code /invsee}.
 *
 * <p>Reads and writes go straight to the target's {@link Inventory}, so the opened chest
 * menu shows and edits the real items instead of a copy.</p>
 */
public final class PlayerInventoryContainer implements Container {
	private final Inventory inventory;

	public PlayerInventoryContainer(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int getContainerSize() {
		return 36;
	}

	@Override
	public boolean isEmpty() {
		return inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return inventory.getItem(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return inventory.removeItem(slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return inventory.removeItemNoUpdate(slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		inventory.setItem(slot, stack);
	}

	@Override
	public void setChanged() {
		inventory.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	/**
	 * Deliberately a no-op: clearing this container would wipe the target player's whole
	 * inventory.
	 */
	@Override
	public void clearContent() {
	}
}
