package com.kaze943.oxycarbide.prompt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kaze943.oxycarbide.Oxycarbide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player choice of where custom prompts are shown, stored in
 * {@code <world>/oxycarbide_promptmode.json}.
 *
 * <p>Only players that picked {@code title} are stored; everybody else uses
 * {@code dialog} (normal chat).</p>
 */
public final class PromptModes {
	public static final String DIALOG = "dialog";
	public static final String TITLE = "title";

	private static final PromptModes INSTANCE = new PromptModes();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, String>>() {
	}.getType();
	private static final String FILE_NAME = "oxycarbide_promptmode.json";

	private final Map<String, String> modes = new LinkedHashMap<>();
	private Path file;

	private PromptModes() {
	}

	public static PromptModes get() {
		return INSTANCE;
	}

	public void load(MinecraftServer server) {
		modes.clear();
		file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);

		if (!Files.exists(file)) {
			return;
		}

		try {
			Map<String, String> loaded = GSON.fromJson(
					Files.readString(file, StandardCharsets.UTF_8), FILE_TYPE);

			if (loaded != null) {
				modes.putAll(loaded);
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read prompt modes from {}", file, e);
		}
	}

	public void save() {
		if (file == null) {
			return;
		}

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(modes, FILE_TYPE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write prompt modes to {}", file, e);
		}
	}

	public void clear() {
		modes.clear();
	}

	/** {@link #DIALOG} or {@link #TITLE}; defaults to {@link #DIALOG}. */
	public String mode(UUID player) {
		String stored = modes.get(player.toString());
		return stored != null && TITLE.equalsIgnoreCase(stored) ? TITLE : DIALOG;
	}

	public boolean isTitle(UUID player) {
		return TITLE.equals(mode(player));
	}

	public void set(UUID player, String mode) {
		if (TITLE.equalsIgnoreCase(mode)) {
			modes.put(player.toString(), TITLE);
		} else {
			modes.remove(player.toString());
		}

		save();
	}
}
