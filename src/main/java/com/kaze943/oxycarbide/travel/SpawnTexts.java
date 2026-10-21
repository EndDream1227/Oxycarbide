package com.kaze943.oxycarbide.travel;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable spawn messages, loaded from
 * {@code config/oxycarbide/lang/zh/spawn_text.json} and
 * {@code config/oxycarbide/lang/en/spawn_text.json}.
 */
public final class SpawnTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"teleport", "已传送到世界出生点");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"teleport", "Teleported to the world spawn.");

	private static final JsonTexts TEXTS = new JsonTexts("spawn_text.json", DEFAULT_ZH, DEFAULT_EN);

	private SpawnTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
