package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.tpa.TpaManager;
import com.kaze943.oxycarbide.tpa.TpaTexts;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /tpa}, {@code /tpahere}, {@code /tpaccept} and {@code /tpadeny}.
 */
public final class TpaCommands {
	private TpaCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("tpa")
					.then(Commands.argument("player", StringArgumentType.word())
							.suggests(Suggestions.PLAYERS)
							.executes(context -> send(context, false))));

			dispatcher.register(Commands.literal("tpahere")
					.then(Commands.argument("player", StringArgumentType.word())
							.suggests(Suggestions.PLAYERS)
							.executes(context -> send(context, true))));

			dispatcher.register(Commands.literal("tpaccept").executes(TpaCommands::accept));
			dispatcher.register(Commands.literal("tpadeny").executes(TpaCommands::deny));
		});
	}

	private static int send(CommandContext<CommandSourceStack> context, boolean here) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String name = StringArgumentType.getString(context, "player");
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(name);

		if (target == null) {
			source.sendFailure(TpaTexts.component("target_not_found", name));
			return 0;
		}

		if (target == player) {
			source.sendFailure(TpaTexts.component("self"));
			return 0;
		}

		TpaManager.Type type = here ? TpaManager.Type.TPAHERE : TpaManager.Type.TPA;
		return TpaManager.get().request(source.getServer(), source, player, target, type) ? 1 : 0;
	}

	private static int accept(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		return TpaManager.get().accept(source.getServer(), player) ? 1 : 0;
	}

	private static int deny(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		return TpaManager.get().deny(source.getServer(), player) ? 1 : 0;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isTpaEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.tpa.disabled"));
		return false;
	}
}
