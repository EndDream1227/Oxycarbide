package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.login.Account;
import com.kaze943.oxycarbide.login.LoginManager;
import com.kaze943.oxycarbide.login.LoginTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * {@code /loginedit}, {@code /loginrequest} and {@code /loginlist}. All operator only.
 */
public final class LoginAdminCommands {
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private LoginAdminCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("loginedit")
					.requires(Admins::isAdmin)
					.then(Commands.argument("first", StringArgumentType.word())
							.then(Commands.argument("second", StringArgumentType.word())
									.then(Commands.argument("third", StringArgumentType.word())
											.executes(LoginAdminCommands::simpleEdit)
											.then(Commands.argument("fourth", StringArgumentType.word())
													.executes(LoginAdminCommands::normalEdit))))));

			dispatcher.register(Commands.literal("loginrequest")
					.requires(Admins::isAdmin)
					.executes(LoginAdminCommands::requests));

			dispatcher.register(Commands.literal("loginlist")
					.requires(Admins::isAdmin)
					.executes(LoginAdminCommands::list));
		});
	}

	private static int simpleEdit(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!OxycarbideConfig.get().isLoginSimpleMode()) {
			source.sendFailure(LoginTexts.component("mode_normal_hint"));
			return 0;
		}

		return report(source, LoginManager.get().editSimple(
				StringArgumentType.getString(context, "first"),
				StringArgumentType.getString(context, "second"),
				StringArgumentType.getString(context, "third")));
	}

	private static int normalEdit(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (OxycarbideConfig.get().isLoginSimpleMode()) {
			source.sendFailure(LoginTexts.component("mode_simple_hint"));
			return 0;
		}

		return report(source, LoginManager.get().editNormal(
				StringArgumentType.getString(context, "first"),
				StringArgumentType.getString(context, "second"),
				StringArgumentType.getString(context, "third"),
				StringArgumentType.getString(context, "fourth")));
	}

	private static int report(CommandSourceStack source, LoginManager.EditResult result) {
		if (result == LoginManager.EditResult.OK) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.login.edit_ok"), true);
			return 1;
		}

		if (result == LoginManager.EditResult.NOT_FOUND) {
			source.sendFailure(Component.translatable("oxycarbide.login.edit_not_found"));
		} else if (result == LoginManager.EditResult.BAD_PASSWORD) {
			source.sendFailure(Component.translatable("oxycarbide.login.edit_bad_password"));
		} else {
			source.sendFailure(Component.translatable("oxycarbide.login.edit_name_taken"));
		}

		return 0;
	}

	private static int requests(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		List<LoginManager.LoginRequest> requests = LoginManager.get().requests();

		if (requests.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.login.request_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.login.request_header", requests.size()), false);

		for (LoginManager.LoginRequest request : requests) {
			String time = TIME_FORMAT.format(Instant.ofEpochMilli(request.time()).atZone(ZoneId.systemDefault()));
			Component result = Component.translatable(request.success()
					? "oxycarbide.login.request_success"
					: "oxycarbide.login.request_failed");

			source.sendSuccess(() -> Component.translatable("oxycarbide.login.request_entry",
					time, request.player(), request.username(), result), false);
		}

		return requests.size();
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		List<Account> accounts = LoginManager.get().accounts();

		if (accounts.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.login.list_empty"), false);
			return 1;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.login.list_header", accounts.size()), false);

		for (Account account : accounts) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.login.list_entry",
					account.username, account.playerName), false);
		}

		return accounts.size();
	}
}
