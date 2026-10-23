package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.mute.MuteManager;
import com.kaze943.oxycarbide.mute.MuteTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /mute}, {@code /fmute} and {@code /unmute}. All three are operator commands.
 *
 * <p>Only online players can be targeted, because the mute is keyed by UUID and needs the
 * player's current name for the messages.</p>
 */
public final class MuteCommands {
	private MuteCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("mute")
					.requires(Admins::isAdmin)
					.then(Commands.argument("player", StringArgumentType.word())
							.suggests(Suggestions.PLAYERS)
							.executes(context -> mute(context, false))));

			dispatcher.register(Commands.literal("fmute")
					.requires(Admins::isAdmin)
					.then(Commands.argument("player", StringArgumentType.word())
							.suggests(Suggestions.PLAYERS)
							.executes(context -> mute(context, true))));

			dispatcher.register(Commands.literal("unmute")
					.requires(Admins::isAdmin)
					.then(Commands.argument("player", StringArgumentType.word())
							.suggests(Suggestions.PLAYERS)
							.executes(MuteCommands::unmute)));
		});
	}

	private static int mute(CommandContext<CommandSourceStack> context, boolean permanent) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer target = findTarget(source, StringArgumentType.getString(context, "player"));

		if (target == null) {
			return 0;
		}

		MuteManager manager = MuteManager.get();

		if (permanent) {
			manager.mutePermanently(target.getUUID(), target.getScoreboardName());
			source.sendSuccess(() -> MuteTexts.component("muted_permanent", target.getScoreboardName()), true);
			target.sendSystemMessage(MuteTexts.component("received_permanent"));
		} else {
			int ticks = OxycarbideConfig.get().getMuteTimer();
			manager.mute(target.getUUID(), target.getScoreboardName(), ticks);
			long seconds = Math.max(1, (ticks + 19) / 20);
			source.sendSuccess(() -> MuteTexts.component("muted", target.getScoreboardName(), seconds), true);
			target.sendSystemMessage(MuteTexts.component("received", seconds));
		}

		manager.save();
		return 1;
	}

	private static int unmute(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer target = findTarget(source, StringArgumentType.getString(context, "player"));

		if (target == null) {
			return 0;
		}

		MuteManager manager = MuteManager.get();

		if (!manager.unmute(target.getUUID())) {
			source.sendFailure(MuteTexts.component("not_muted", target.getScoreboardName()));
			return 0;
		}

		manager.save();
		source.sendSuccess(() -> MuteTexts.component("unmuted", target.getScoreboardName()), true);
		target.sendSystemMessage(MuteTexts.component("lifted"));
		return 1;
	}

	private static ServerPlayer findTarget(CommandSourceStack source, String name) {
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(name);

		if (target == null) {
			source.sendFailure(Component.translatable("oxycarbide.mute.target_not_found", name));
		}

		return target;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (OxycarbideConfig.get().isMuteEnabled()) {
			return true;
		}

		source.sendFailure(Component.translatable("oxycarbide.mute.disabled"));
		return false;
	}
}
