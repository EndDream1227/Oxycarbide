package com.kaze943.oxycarbide.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/**
 * Chat components shared by the home and warp commands.
 */
public final class TextComponents {
	private TextComponents() {
	}

	/** Coordinates are always printed the same way, with a dot and one decimal. */
	public static String formatCoordinate(double value) {
		return String.format(Locale.ROOT, "%.1f", value);
	}

	/**
	 * Green, bracketed coordinates like the ones {@code /locate} prints. Clicking them
	 * fills {@code /tp @s <x> <y> <z>} into the chat bar.
	 */
	public static Component clickableCoordinates(double x, double y, double z) {
		String xs = formatCoordinate(x);
		String ys = formatCoordinate(y);
		String zs = formatCoordinate(z);

		return ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", xs, ys, zs))
				.withStyle(style -> style
						.withColor(ChatFormatting.GREEN)
						.withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + xs + " " + ys + " " + zs))
						.withHoverEvent(new HoverEvent.ShowText(
								Component.translatable("oxycarbide.coordinates.tooltip"))));
	}

	/**
	 * A coloured, bracketed button that runs a command when clicked.
	 *
	 * @param labelKey translation key of the label, e.g. {@code oxycarbide.tpa.accept}
	 * @param command  command to run on click, e.g. {@code /tpaccept}
	 * @param color    label colour
	 * @param hoverKey translation key of the hover tooltip
	 */
	public static Component clickableCommand(String labelKey, String command, ChatFormatting color, String hoverKey) {
		MutableComponent label = Component.translatable(labelKey)
				.withStyle(style -> style.withColor(color));

		return ComponentUtils.wrapInSquareBrackets(label)
				.withStyle(style -> style
						.withClickEvent(new ClickEvent.RunCommand(command))
						.withHoverEvent(new HoverEvent.ShowText(Component.translatable(hoverKey))));
	}
}
