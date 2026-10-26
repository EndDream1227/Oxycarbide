package com.kaze943.oxycarbide.title;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns the {@code color} field of a title into a component.
 *
 * <p>Three forms are accepted:</p>
 *
 * <pre>
 * gold                              named vanilla colour
 * #ffaa00   0xffaa00                 single RGB colour
 * #ff0000,#00ff00,#0000ff           gradient, one colour per stop, spread over the text
 * </pre>
 */
public final class TitleColors {
	private static final Map<String, ChatFormatting> NAMED = Map.ofEntries(
			Map.entry("black", ChatFormatting.BLACK),
			Map.entry("dark_blue", ChatFormatting.DARK_BLUE),
			Map.entry("dark_green", ChatFormatting.DARK_GREEN),
			Map.entry("dark_aqua", ChatFormatting.DARK_AQUA),
			Map.entry("dark_red", ChatFormatting.DARK_RED),
			Map.entry("dark_purple", ChatFormatting.DARK_PURPLE),
			Map.entry("gold", ChatFormatting.GOLD),
			Map.entry("gray", ChatFormatting.GRAY),
			Map.entry("grey", ChatFormatting.GRAY),
			Map.entry("dark_gray", ChatFormatting.DARK_GRAY),
			Map.entry("dark_grey", ChatFormatting.DARK_GRAY),
			Map.entry("blue", ChatFormatting.BLUE),
			Map.entry("green", ChatFormatting.GREEN),
			Map.entry("aqua", ChatFormatting.AQUA),
			Map.entry("red", ChatFormatting.RED),
			Map.entry("light_purple", ChatFormatting.LIGHT_PURPLE),
			Map.entry("yellow", ChatFormatting.YELLOW),
			Map.entry("white", ChatFormatting.WHITE));

	private TitleColors() {
	}

	/** {@code true} when the text can be parsed as a title colour. */
	public static boolean isValid(String color) {
		return gradientOf(color) != null || namedOf(color) != null || rgbOf(color) != null;
	}

	/** Renders {@code text} in the given colour; gradients colour every character. */
	public static Component render(String text, String color) {
		String value = text == null ? "" : text;

		if (value.isEmpty()) {
			return Component.empty();
		}

		List<Integer> gradient = gradientOf(color);

		if (gradient != null) {
			return gradientText(value, gradient);
		}

		ChatFormatting named = namedOf(color);

		if (named != null) {
			return Component.literal(value).withStyle(style -> style.withColor(named));
		}

		Integer rgb = rgbOf(color);

		if (rgb != null) {
			return Component.literal(value).withStyle(style -> style.withColor(TextColor.fromRgb(rgb)));
		}

		return Component.literal(value);
	}

	/** A comma separated list of RGB colours, or {@code null} when it is not a gradient. */
	private static List<Integer> gradientOf(String color) {
		if (color == null || !color.contains(",")) {
			return null;
		}

		List<Integer> stops = new ArrayList<>();
		String[] parts = color.split(",");

		for (String part : parts) {
			Integer rgb = rgbOf(part);

			if (rgb == null) {
				return null;
			}

			stops.add(rgb);
		}

		return stops.isEmpty() ? null : stops;
	}

	private static ChatFormatting namedOf(String color) {
		if (color == null || color.isBlank()) {
			return null;
		}

		String key = color.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
		return NAMED.get(key);
	}

	private static Integer rgbOf(String color) {
		if (color == null) {
			return null;
		}

		String value = color.trim();
		String hex;

		if (value.startsWith("#")) {
			hex = value.substring(1);
		} else if (value.startsWith("0x") || value.startsWith("0X")) {
			hex = value.substring(2);
		} else {
			return null;
		}

		if (hex.length() != 6) {
			return null;
		}

		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Component gradientText(String text, List<Integer> stops) {
		MutableComponent result = Component.empty();
		StringBuilder run = new StringBuilder();
		int runColor = -1;
		int length = text.length();

		for (int index = 0; index < length; index++) {
			double progress = length <= 1 ? 0.0 : (double) index / (length - 1);
			int rgb = colorAt(stops, progress);

			if (rgb != runColor) {
				flush(result, run, runColor);
				runColor = rgb;
			}

			run.append(text.charAt(index));
		}

		flush(result, run, runColor);
		return result;
	}

	private static void flush(MutableComponent result, StringBuilder run, int rgb) {
		if (run.length() > 0 && rgb >= 0) {
			result.append(Component.literal(run.toString())
					.withStyle(style -> style.withColor(TextColor.fromRgb(rgb))));
			run.setLength(0);
		}
	}

	private static int colorAt(List<Integer> stops, double progress) {
		if (stops.size() == 1) {
			return stops.get(0);
		}

		double scaled = Math.max(0.0, Math.min(1.0, progress)) * (stops.size() - 1);
		int index = (int) Math.floor(scaled);

		if (index >= stops.size() - 1) {
			return stops.get(stops.size() - 1);
		}

		return mix(stops.get(index), stops.get(index + 1), scaled - index);
	}

	private static int mix(int from, int to, double ratio) {
		int red = channel(from, 16, to, ratio);
		int green = channel(from, 8, to, ratio);
		int blue = channel(from, 0, to, ratio);
		return (red << 16) | (green << 8) | blue;
	}

	private static int channel(int from, int shift, int to, double ratio) {
		int start = (from >> shift) & 0xFF;
		int end = (to >> shift) & 0xFF;
		int value = (int) Math.round(start + (end - start) * ratio);
		return Math.max(0, Math.min(255, value));
	}
}
