package com.kaze943.oxycarbide.menu;

import com.kaze943.oxycarbide.Oxycarbide;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds menus, renders their buttons and performs the button actions.
 */
public final class MenuService {
	private static final MenuService INSTANCE = new MenuService();

	private MenuService() {
	}

	public static MenuService get() {
		return INSTANCE;
	}

	public void open(ServerPlayer player, String id) {
		MenuDefinition menu = MenuManager.get().get(id);

		if (menu == null) {
			player.sendSystemMessage(MenuTexts.component("not_found", id));
			return;
		}

		open(player, menu);
	}

	public void open(ServerPlayer player, MenuDefinition menu) {
		int rows = Math.max(1, Math.min(6, menu.rows));
		int size = rows * 9;
		SimpleContainer display = new SimpleContainer(size);

		for (MenuButton button : menu.buttons) {
			int slot = MenuDefinition.slotOf(button);

			if (slot >= 0 && slot < size) {
				display.setItem(slot, buildStack(button));
			}
		}

		String title = menu.title == null || menu.title.isBlank() ? menu.id : menu.title;

		player.openMenu(new SimpleMenuProvider(
				(containerId, playerInventory, opener) -> new MenuMenu(containerId, playerInventory, menu,
						display, rows, menuType(rows)),
				Component.literal(title)));
	}

	private static MenuType<?> menuType(int rows) {
		return switch (rows) {
			case 1 -> MenuType.GENERIC_9x1;
			case 2 -> MenuType.GENERIC_9x2;
			case 3 -> MenuType.GENERIC_9x3;
			case 4 -> MenuType.GENERIC_9x4;
			case 5 -> MenuType.GENERIC_9x5;
			default -> MenuType.GENERIC_9x6;
		};
	}

	private ItemStack buildStack(MenuButton button) {
		ItemStack stack = new ItemStack(itemOf(button.item));

		if (button.name != null && !button.name.isBlank()) {
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(button.name));
		}

		List<Component> lore = new ArrayList<>();

		if (button.lore != null) {
			for (String line : button.lore) {
				if (line != null && !line.isBlank()) {
					lore.add(Component.literal(line));
				}
			}
		}

		if (!lore.isEmpty()) {
			stack.set(DataComponents.LORE, new ItemLore(lore));
		}

		if (button.enchanted) {
			stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}

		return stack;
	}

	/** Called from {@link MenuMenu} for left clicks on a menu slot. */
	public void handleClick(ServerPlayer player, MenuDefinition menu, int slot) {
		if (!OxycarbideConfig.get().isMenuEnabled()) {
			// Switched off while the GUI was open: close it instead of running the button.
			player.closeContainer();
			return;
		}

		MenuButton button = menu.atSlot(slot);

		if (button == null) {
			return;
		}

		switch (button.actionLower()) {
			case "command", "run" -> runCommand(player, button.value);
			case "console" -> runAsConsole(player, button.value);
			case "menu", "open", "page" -> openTarget(player, button.value);
			default -> {
				// placeholder: no reaction
			}
		}
	}

	/** Runs the command as the clicking player, so that permissions still apply. */
	private void runCommand(ServerPlayer player, String raw) {
		String command = clean(raw);

		if (command.isEmpty()) {
			return;
		}

		// Close first: the command may open another container or teleport the player.
		player.closeContainer();
		CommandSourceStack source = player.createCommandSourceStack();

		try {
			source.getServer().getCommands().performPrefixedCommand(source, command);
		} catch (RuntimeException e) {
			Oxycarbide.LOGGER.warn("Menu command /{} failed for {}", command, player.getScoreboardName(), e);
		}
	}

	/**
	 * Runs the command with server permissions, for buttons like {@code /time set day}.
	 * Such a command has no player attached, so player-only commands do not work here.
	 */
	private void runAsConsole(ServerPlayer player, String raw) {
		String command = clean(raw);

		if (command.isEmpty()) {
			return;
		}

		player.closeContainer();
		MinecraftServer server = player.createCommandSourceStack().getServer();

		try {
			server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
		} catch (RuntimeException e) {
			Oxycarbide.LOGGER.warn("Menu console command /{} failed", command, e);
		}
	}

	/** Strips the optional leading slash and surrounding blanks. */
	private static String clean(String raw) {
		String command = raw == null ? "" : raw.trim();
		return command.startsWith("/") ? command.substring(1).trim() : command;
	}

	private void openTarget(ServerPlayer player, String target) {
		if (target == null || target.isBlank()) {
			return;
		}

		MenuDefinition menu = MenuManager.get().get(target);

		if (menu == null) {
			player.sendSystemMessage(MenuTexts.component("not_found", target));
			return;
		}

		player.closeContainer();
		open(player, menu);
	}

	private static Item itemOf(String id) {
		if (id == null || id.isBlank()) {
			return Items.STONE;
		}

		Identifier identifier = Identifier.tryParse(id);
		Item item = identifier == null ? null : BuiltInRegistries.ITEM.getValue(identifier);
		return item == null ? Items.STONE : item;
	}
}
