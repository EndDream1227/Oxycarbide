package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.nickname.NicknameManager;
import com.kaze943.oxycarbide.nickname.NicknameTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /nickname <name>} sets your own chat nickname, {@code /nickfor <player> <name>}
 * sets somebody else's (operator only).
 */
public final class NicknameCommand {
	private NicknameCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("nickname")
					.then(Commands.argument("name", StringArgumentType.greedyString())
							.executes(NicknameCommand::setNickname)));

			dispatcher.register(Commands.literal("nickfor")
					.requires(Admins::isAdmin)
					.then(Commands.argument("player", StringArgumentType.word())
							.then(Commands.argument("name", StringArgumentType.greedyString())
									.executes(NicknameCommand::setOtherNickname))));
		});
	}

	private static int setNickname(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String nickname = StringArgumentType.getString(context, "name").trim();

		if (!isValid(source, nickname)) {
			return 0;
		}

		NicknameManager.get().set(player.getUUID(), nickname);
		NicknameManager.get().save();

		source.sendSuccess(() -> NicknameTexts.component("set", nickname), false);
		return 1;
	}

	private static int setOtherNickname(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		String targetName = StringArgumentType.getString(context, "player");
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);

		if (target == null) {
			source.sendFailure(NicknameTexts.component("target_not_found", targetName));
			return 0;
		}

		String nickname = StringArgumentType.getString(context, "name").trim();

		if (!isValid(source, nickname)) {
			return 0;
		}

		NicknameManager.get().set(target.getUUID(), nickname);
		NicknameManager.get().save();

		source.sendSuccess(() -> NicknameTexts.component("set_other",
				target.getScoreboardName(), nickname), true);
		target.sendSystemMessage(NicknameTexts.component("received", nickname));
		return 1;
	}

	private static boolean isValid(CommandSourceStack source, String nickname) {
		if (nickname.isEmpty() || nickname.length() > NicknameManager.MAX_LENGTH) {
			source.sendFailure(NicknameTexts.component("invalid", NicknameManager.MAX_LENGTH));
			return false;
		}

		return true;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isNicknameEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.nickname.disabled"));
		return false;
	}
}
