package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.login.LoginManager;
import com.kaze943.oxycarbide.login.LoginTexts;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * {@code /login}: registers the account on the first use and logs in afterwards.
 *
 * <p>Normal mode takes {@code <username> <password>}; simple mode takes only
 * {@code <password>} and uses the player name as the username.</p>
 */
public final class LoginCommand {
	private LoginCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("login")
						.then(Commands.argument("first", StringArgumentType.word())
								.executes(LoginCommand::simpleForm)
								.then(Commands.argument("second", StringArgumentType.word())
										.executes(LoginCommand::normalForm)))));
	}

	private static int simpleForm(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		if (!OxycarbideConfig.get().isLoginSimpleMode()) {
			source.sendFailure(LoginTexts.component("mode_normal_hint"));
			return 0;
		}

		String password = StringArgumentType.getString(context, "first");
		return complete(source, player, player.getScoreboardName(), password, true);
	}

	private static int normalForm(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		if (!ensureEnabled(source)) {
			return 0;
		}

		if (OxycarbideConfig.get().isLoginSimpleMode()) {
			source.sendFailure(LoginTexts.component("mode_simple_hint"));
			return 0;
		}

		String username = StringArgumentType.getString(context, "first");
		String password = StringArgumentType.getString(context, "second");
		return complete(source, player, username, password, false);
	}

	private static int complete(CommandSourceStack source, ServerPlayer player, String username, String password, boolean simple) {
		LoginManager manager = LoginManager.get();
		UUID uuid = player.getUUID();
		String playerName = player.getScoreboardName();

		if (manager.isAuthenticated(uuid)) {
			source.sendFailure(Component.translatable("oxycarbide.login.already"));
			return 0;
		}

		if (!manager.hasAccount(uuid)) {
			if (!manager.register(uuid, playerName, username, password)) {
				manager.recordRequest(playerName, username, false);
				source.sendFailure(LoginTexts.component("username_taken"));
				return 0;
			}
		} else if (!manager.authenticate(uuid, username, password, simple)) {
			manager.recordRequest(playerName, username, false);
			source.sendFailure(LoginTexts.component("wrong"));
			return 0;
		}

		manager.markAuthenticated(uuid);
		manager.recordRequest(playerName, username, true);
		source.sendSuccess(() -> LoginTexts.component("success", simple ? playerName : username), false);
		return 1;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isLoginEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.login.disabled"));
		return false;
	}
}
