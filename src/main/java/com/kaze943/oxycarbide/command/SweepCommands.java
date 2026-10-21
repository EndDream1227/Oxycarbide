package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.sweep.SweepManager;
import com.kaze943.oxycarbide.sweep.SweepTexts;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * {@code /sweep}: removes dropped items (and monsters in entity mode) right now.
 */
public final class SweepCommands {
	private SweepCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("sweep")
						.requires(Admins::isAdmin)
						.executes(SweepCommands::sweep)));
	}

	private static int sweep(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		if (!config.isSweepEnabled()) {
			source.sendFailure(Component.translatable("oxycarbide.sweep.disabled"));
			return 0;
		}

		SweepManager manager = SweepManager.get();
		SweepManager.SweepResult result = manager.sweepNow(source.getServer());

		// Players already receive the broadcast; only console/command blocks need a reply,
		// otherwise the message would show up twice for the executing player.
		manager.announce(source.getServer(), config, result);

		if (source.getPlayer() == null) {
			String text = "entity".equalsIgnoreCase(config.getSweepMode())
					? SweepTexts.text("clear_entity", result.items(), result.mobs())
					: SweepTexts.text("clear", result.items());
			source.sendSuccess(() -> Component.literal(text), true);
		}

		return Math.max(1, result.items() + result.mobs());
	}
}
