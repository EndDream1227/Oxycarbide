package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.sidebar.CounterManager;
import com.kaze943.oxycarbide.sidebar.SidebarTexts;
import com.kaze943.oxycarbide.sidebar.SidebarManager;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /oxycarbide scoreboard ...}: shows the state of the sidebar, reloads its JSON and
 * edits the counters used by the {@code custom} line type.
 *
 * <p>The on/off switch lives next to the other modules in
 * {@code /oxycarbide config scoreboard switch}.</p>
 */
public final class SidebarCommands {
	private SidebarCommands() {
	}

	public static LiteralArgumentBuilder<CommandSourceStack> node() {
		return Commands.literal("scoreboard")
				.executes(SidebarCommands::status)
				.then(Commands.literal("reload")
						.requires(Admins::isAdmin)
						.executes(SidebarCommands::reload))
				.then(Commands.literal("counter")
						.requires(Admins::isAdmin)
						.then(Commands.literal("set")
								.then(Commands.argument("counter", StringArgumentType.word())
										.suggests(Suggestions.COUNTERS)
										.then(Commands.argument("player", StringArgumentType.word())
												.suggests(Suggestions.PLAYERS)
												.then(Commands.argument("value", LongArgumentType.longArg())
														.executes(context -> counter(context, true))))))
						.then(Commands.literal("add")
								.then(Commands.argument("counter", StringArgumentType.word())
										.suggests(Suggestions.COUNTERS)
										.then(Commands.argument("player", StringArgumentType.word())
												.suggests(Suggestions.PLAYERS)
												.then(Commands.argument("amount", LongArgumentType.longArg())
														.executes(context -> counter(context, false)))))));
	}

	private static int status(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.scoreboard.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isSidebarEnabled()
				? "oxycarbide.config.scoreboard.status_enabled"
				: "oxycarbide.config.scoreboard.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.scoreboard.lines_status",
				SidebarManager.get().settings().lines.size()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.scoreboard.update_status",
				SidebarManager.get().settings().updateTicks), false);
		return 1;
	}

	private static int reload(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		SidebarManager.get().load();
		source.sendSuccess(() -> SidebarTexts.component("reloaded",
				SidebarManager.get().settings().lines.size()), true);
		return 1;
	}

	private static int counter(CommandContext<CommandSourceStack> context, boolean set) {
		CommandSourceStack source = context.getSource();
		String counter = CounterManager.normalize(StringArgumentType.getString(context, "counter"));
		String playerName = StringArgumentType.getString(context, "player");
		long amount = set
				? LongArgumentType.getLong(context, "value")
				: LongArgumentType.getLong(context, "amount");
		ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);

		if (target == null) {
			source.sendFailure(SidebarTexts.component("target_missing", playerName));
			return 0;
		}

		long result = set
				? CounterManager.get().set(counter, target.getUUID(), target.getScoreboardName(), amount)
				: CounterManager.get().add(counter, target.getUUID(), target.getScoreboardName(), amount);

		source.sendSuccess(() -> SidebarTexts.component(set
				? "counter_set"
				: "counter_added", counter, target.getScoreboardName(), result), true);
		return 1;
	}
}
