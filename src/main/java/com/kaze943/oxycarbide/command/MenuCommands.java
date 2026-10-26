package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.menu.MenuDefinition;
import com.kaze943.oxycarbide.menu.MenuManager;
import com.kaze943.oxycarbide.menu.MenuService;
import com.kaze943.oxycarbide.menu.MenuTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * {@code /menu [id]}, {@code /menu_default}, {@code /menu_create}, {@code /menu_list}
 * and {@code /menu_reload}.
 *
 * <p>Menus are JSON files in {@code config/oxycarbide/menu/}; see the README for the
 * structure.</p>
 */
public final class MenuCommands {
	private MenuCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("menu")
					.executes(MenuCommands::openDefault)
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(Suggestions.MENUS)
							.executes(MenuCommands::open)));

			dispatcher.register(Commands.literal("menu_default")
					.requires(Admins::isAdmin)
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(Suggestions.MENUS)
							.executes(MenuCommands::setDefault)));

			dispatcher.register(Commands.literal("menu_create")
					.requires(Admins::isAdmin)
					.executes(context -> create(context, "~"))
					.then(Commands.argument("id", StringArgumentType.word())
							.executes(context -> create(context, StringArgumentType.getString(context, "id")))));

			dispatcher.register(Commands.literal("menu_list")
					.requires(Admins::isAdmin)
					.executes(MenuCommands::list));

			dispatcher.register(Commands.literal("menu_reload")
					.requires(Admins::isAdmin)
					.executes(MenuCommands::reload));
		});
	}

	/** {@code /menu}: opens the menu configured by {@code /menu_default}. */
	private static int openDefault(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		String id = OxycarbideConfig.get().getMenuDefaultId();

		if (id == null || id.isBlank()) {
			player.sendSystemMessage(MenuTexts.component("no_default"));
			return 0;
		}

		MenuService.get().open(player, id);
		return 1;
	}

	private static int open(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		MenuService.get().open(player, StringArgumentType.getString(context, "id"));
		return 1;
	}

	private static int setDefault(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String id = StringArgumentType.getString(context, "id");

		if (!MenuManager.get().contains(id)) {
			source.sendFailure(MenuTexts.component("default_missing", id));
			return 0;
		}

		OxycarbideConfig.get().setMenuDefaultId(id);
		source.sendSuccess(() -> MenuTexts.component("default_set", id), true);
		return 1;
	}

	private static int create(CommandContext<CommandSourceStack> context, String id) {
		CommandSourceStack source = context.getSource();
		MenuDefinition created = MenuManager.get().create(id);

		if (created == null) {
			source.sendFailure(MenuTexts.component("create_failed", id));
			return 0;
		}

		source.sendSuccess(() -> MenuTexts.component("created", created.id), true);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		List<MenuDefinition> menus = MenuManager.get().all();

		if (menus.isEmpty()) {
			source.sendSuccess(() -> MenuTexts.component("list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> MenuTexts.component("list_header", menus.size()), false);

		for (MenuDefinition menu : menus) {
			source.sendSuccess(() -> MenuTexts.component("list_entry", menu.id, menu.title), false);
		}

		return menus.size();
	}

	private static int reload(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		MenuManager.get().load();
		MenuTexts.load();
		source.sendSuccess(() -> MenuTexts.component("reloaded", MenuManager.get().size()), true);
		return 1;
	}

	/**
	 * {@code /menu} and {@code /menu <id>} refuse to run while
	 * {@code /oxycarbide config menu switch false} is active; the file management commands
	 * keep working so menus can be prepared while the feature is off.
	 */
	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isMenuEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.menu.disabled"));
		return false;
	}
}
