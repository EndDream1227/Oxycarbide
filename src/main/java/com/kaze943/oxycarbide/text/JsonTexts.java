package com.kaze943.oxycarbide.text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Messages that server owners can edit from the config directory.
 *
 * <p>Every instance loads two files:</p>
 *
 * <pre>
 * config/oxycarbide/lang/zh/&lt;file&gt;
 * config/oxycarbide/lang/en/&lt;file&gt;
 * </pre>
 *
 * <p>Missing files are generated with the built-in defaults, and keys missing from an
 * existing file also fall back to those defaults.</p>
 *
 * <p>Templates use {@code %s} placeholders and {@code %%} for a literal percent sign.
 * {@link #component} keeps non-string arguments, such as clickable coordinate
 * components, intact, while {@link #text} turns everything into a plain string.</p>
 */
public final class JsonTexts {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {
	}.getType();
	private static final String DIRECTORY = "oxycarbide/lang";

	private final String fileName;
	private final Map<String, String> defaultChinese;
	private final Map<String, String> defaultEnglish;
	private Map<String, String> chinese;
	private Map<String, String> english;

	public JsonTexts(String fileName, Map<String, String> defaultChinese, Map<String, String> defaultEnglish) {
		this.fileName = fileName;
		this.defaultChinese = defaultChinese;
		this.defaultEnglish = defaultEnglish;
		this.chinese = new LinkedHashMap<>(defaultChinese);
		this.english = new LinkedHashMap<>(defaultEnglish);
	}

	/** Builds an ordered defaults map from {@code key, value, key, value, ...} pairs. */
	public static Map<String, String> defaults(String... pairs) {
		Map<String, String> map = new LinkedHashMap<>();

		for (int i = 0; i + 1 < pairs.length; i += 2) {
			map.put(pairs[i], pairs[i + 1]);
		}

		return map;
	}

	public void load() {
		Path directory = FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY);
		chinese = loadFile(directory.resolve("zh").resolve(fileName), defaultChinese);
		english = loadFile(directory.resolve("en").resolve(fileName), defaultEnglish);
	}

	/** The formatted template, as a plain string. */
	public String text(String key, Object... args) {
		String template = raw(key);
		StringBuilder builder = new StringBuilder(template.length() + 16);
		int argIndex = 0;
		int index = 0;

		while (index < template.length()) {
			char character = template.charAt(index);

			if (character == '%' && index + 1 < template.length()) {
				char next = template.charAt(index + 1);

				if (next == 's') {
					if (argIndex < args.length) {
						builder.append(args[argIndex++]);
					}

					index += 2;
					continue;
				}

				if (next == '%') {
					builder.append('%');
					index += 2;
					continue;
				}
			}

			builder.append(character);
			index++;
		}

		return builder.toString();
	}

	/** The formatted template, keeping {@link Component} arguments (hover/click data) intact. */
	public Component component(String key, Object... args) {
		String template = raw(key);
		MutableComponent result = Component.empty();
		StringBuilder literal = new StringBuilder();
		int argIndex = 0;
		int index = 0;

		while (index < template.length()) {
			char character = template.charAt(index);

			if (character == '%' && index + 1 < template.length()) {
				char next = template.charAt(index + 1);

				if (next == 's') {
					flush(result, literal);

					if (argIndex < args.length) {
						Object arg = args[argIndex++];
						result.append(arg instanceof Component component
								? component
								: Component.literal(String.valueOf(arg)));
					}

					index += 2;
					continue;
				}

				if (next == '%') {
					literal.append('%');
					index += 2;
					continue;
				}
			}

			literal.append(character);
			index++;
		}

		flush(result, literal);
		return result;
	}

	private static void flush(MutableComponent result, StringBuilder literal) {
		if (literal.length() > 0) {
			result.append(Component.literal(literal.toString()));
			literal.setLength(0);
		}
	}

	private String raw(String key) {
		String value = (isChinese() ? chinese : english).get(key);

		if (value != null) {
			return value;
		}

		return (isChinese() ? defaultChinese : defaultEnglish).getOrDefault(key, key);
	}

	private static boolean isChinese() {
		return Locale.getDefault().getLanguage().toLowerCase(Locale.ROOT).startsWith("zh");
	}

	private static Map<String, String> loadFile(Path path, Map<String, String> defaults) {
		if (!Files.exists(path)) {
			write(path, defaults);
			return new LinkedHashMap<>(defaults);
		}

		try {
			Map<String, String> loaded = GSON.fromJson(
					Files.readString(path, StandardCharsets.UTF_8), MAP_TYPE);
			Map<String, String> result = new LinkedHashMap<>(defaults);

			if (loaded != null) {
				result.putAll(loaded);
			}

			return result;
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read the text file {}", path, e);
			return new LinkedHashMap<>(defaults);
		}
	}

	private static void write(Path path, Map<String, String> defaults) {
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(defaults), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write the text file {}", path, e);
		}
	}
}
