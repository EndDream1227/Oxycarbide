package com.kaze943.oxycarbide.mixin;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.sidebar.SidebarTexts;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Refuses the vanilla sidebar commands while the oxycarbide sidebar is on, because the
 * two would fight over the same display slot.
 *
 * <p>Only the sidebar slots are blocked; teams, objectives and scores keep working, and
 * nothing happens while the feature is switched off.</p>
 */
@Mixin(ScoreboardCommand.class)
public abstract class SidebarCommandMixin {
	private static final String SET_DISPLAY =
			"setDisplaySlot(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/world/scores/DisplaySlot;Lnet/minecraft/world/scores/Objective;)I";
	private static final String CLEAR_DISPLAY =
			"clearDisplaySlot(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/world/scores/DisplaySlot;)I";

	@Inject(method = SET_DISPLAY, at = @At("HEAD"), cancellable = true)
	private static void oxycarbide$blockSetDisplay(CommandSourceStack source, DisplaySlot slot,
			Objective objective, CallbackInfoReturnable<Integer> callback) {
		if (oxycarbide$blocked(source, slot)) {
			callback.setReturnValue(0);
		}
	}

	@Inject(method = CLEAR_DISPLAY, at = @At("HEAD"), cancellable = true)
	private static void oxycarbide$blockClearDisplay(CommandSourceStack source, DisplaySlot slot,
			CallbackInfoReturnable<Integer> callback) {
		if (oxycarbide$blocked(source, slot)) {
			callback.setReturnValue(0);
		}
	}

	private static boolean oxycarbide$blocked(CommandSourceStack source, DisplaySlot slot) {
		if (slot == null || !OxycarbideConfig.get().isSidebarEnabled()) {
			return false;
		}

		String name = slot.getSerializedName();

		if (name == null || !name.startsWith("sidebar")) {
			return false;
		}

		source.sendFailure(SidebarTexts.component("occupied"));
		return true;
	}
}
