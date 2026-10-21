package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.shop.ShopDefinition;
import com.kaze943.oxycarbide.shop.ShopManager;
import com.kaze943.oxycarbide.shop.ShopService;
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
 * {@code /shop [id]}, {@code /shop_create}, {@code /shop_settings default},
 * {@code /shoplist} and {@code /shopreload}.
 */
public final class ShopCommands {
	private ShopCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("shop")
					.executes(ShopCommands::openDefault)
					.then(Commands.argument("id", StringArgumentType.word())
							.executes(ShopCommands::open)));

			dispatcher.register(Commands.literal("shop_create")
					.requires(Admins::isAdmin)
					.then(Commands.argument("name", StringArgumentType.string())
							.then(Commands.argument("id", StringArgumentType.word())
									.executes(ShopCommands::create))));

			dispatcher.register(Commands.literal("shop_settings")
					.requires(Admins::isAdmin)
					.then(Commands.literal("default")
							.then(Commands.argument("id", StringArgumentType.word())
									.executes(ShopCommands::setDefault))));

			dispatcher.register(Commands.literal("shoplist")
					.requires(Admins::isAdmin)
					.executes(ShopCommands::list));

			dispatcher.register(Commands.literal("shopreload")
					.requires(Admins::isAdmin)
					.executes(ShopCommands::reload));
		});
	}

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

		String id = OxycarbideConfig.get().getShopDefaultId();

		if (id == null || id.isBlank()) {
			source.sendFailure(Component.translatable("oxycarbide.shop.default_missing"));
			return 0;
		}

		if (!ShopManager.get().contains(id)) {
			source.sendFailure(Component.translatable("oxycarbide.shop.default_not_found", id));
			return 0;
		}

		ShopService.get().open(player, id);
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

		ShopService.get().open(player, StringArgumentType.getString(context, "id"));
		return 1;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isShopEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.shop.disabled"));
		return false;
	}

	private static int create(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String name = StringArgumentType.getString(context, "name");
		String id = StringArgumentType.getString(context, "id");
		ShopDefinition shop = ShopManager.get().create(name, id);

		if (shop == null) {
			source.sendFailure(Component.translatable("oxycarbide.shop.create_failed", id));
			return 0;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.shop.created", shop.title, shop.id), true);
		return 1;
	}

	private static int setDefault(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String id = StringArgumentType.getString(context, "id");

		if (!ShopManager.get().contains(id)) {
			source.sendFailure(Component.translatable("oxycarbide.shop.not_found", id));
			return 0;
		}

		OxycarbideConfig.get().setShopDefaultId(id);
		source.sendSuccess(() -> Component.translatable("oxycarbide.shop.default_set", id), true);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		List<ShopDefinition> shops = ShopManager.get().all();

		if (shops.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.shop.list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.shop.list_header", shops.size()), false);

		for (ShopDefinition shop : shops) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.shop.list_entry", shop.id, shop.title), false);
		}

		return shops.size();
	}

	private static int reload(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ShopManager.get().load();
		source.sendSuccess(() -> Component.translatable("oxycarbide.shop.reloaded", ShopManager.get().size()), true);
		return 1;
	}
}
