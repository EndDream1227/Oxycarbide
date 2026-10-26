package com.kaze943.oxycarbide.sidebar;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Turns the strings of {@code scoreboard.json} into components: {@code &} (or {@code §})
 * colour codes become real styles, and {@code %name%} placeholders are replaced first.
 */
public final class SidebarText {
	private SidebarText() {
	}

	/** Replaces {@code %key%} with the given value; {@code pairs} is key, value, key, ... */
	public static String fill(String template, String... pairs) {
		if (template == null || template.isEmpty() || pairs.length == 0) {
			return template == null ? "" : template;
		}

		String result = template;

		for (int index = 0; index + 1 < pairs.length; index += 2) {
			result = result.replace("%" + pairs[index] + "%", pairs[index + 1]);
		}

		return result;
	}

	/** Parses {@code &} colour and style codes into a styled component. */
	public static Component parse(String text) {
		MutableComponent result = Component.empty();

		if (text == null || text.isEmpty()) {
			return result;
		}

		Style style = Style.EMPTY;
		StringBuilder buffer = new StringBuilder();

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);

			if ((character == '&' || character == '\u00a7') && index + 1 < text.length()) {
				ChatFormatting formatting = ChatFormatting.getByCode(Character.toLowerCase(text.charAt(index + 1)));

				if (formatting != null) {
					flush(result, buffer, style);
					style = apply(style, formatting);
					index++;
					continue;
				}
			}

			buffer.append(character);
		}

		flush(result, buffer, style);
		return result;
	}

	private static void flush(MutableComponent result, StringBuilder buffer, Style style) {
		if (buffer.length() > 0) {
			result.append(Component.literal(buffer.toString()).withStyle(style));
			buffer.setLength(0);
		}
	}

	private static Style apply(Style style, ChatFormatting formatting) {
		return switch (formatting) {
			case BOLD -> style.withBold(true);
			case ITALIC -> style.withItalic(true);
			case UNDERLINE -> style.withUnderlined(true);
			case STRIKETHROUGH -> style.withStrikethrough(true);
			case OBFUSCATED -> style.withObfuscated(true);
			case RESET -> Style.EMPTY;
			default -> style.withColor(formatting);
		};
	}
}
