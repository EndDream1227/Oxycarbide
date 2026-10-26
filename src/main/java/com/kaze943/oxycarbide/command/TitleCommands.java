package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.title.TitleColors;
import com.kaze943.oxycarbide.title.TitleDefinition;
import com.kaze943.oxycarbide.title.TitleTexts;
import com.kaze943.oxycarbide.title.TitlesManager;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /titles}: creates titles, hands them out and wears them.
 *
 * <p>{@code /titles create} and {@code /titles get} are operator commands; {@code wear} and
 * {@code clear} are open to everyone. Shops can sell titles with a {@code command} item
 * that runs {@code /title get <id> %player%} (see the README).</p>
 */
public final class TitleCommands {
	private TitleCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("titles")
					.executes(TitleCommands::current)
					.then(Commands.literal("create")
							.requires(Admins::isAdmin)
							.then(Commands.argument("text", StringArgumentType.string())
									.then(Commands.argument("color", StringArgumentType.word())
											.then(Commands.argument("id", StringArgumentType.word())
													.executes(TitleCommands::create)))))
					.then(Commands.literal("get")
							.requires(Admins::isAdmin)
							.then(Commands.argument("id", StringArgumentType.word())
									.suggests(Suggestions.TITLES)
									.executes(context -> obtain(context, null))
									.then(Commands.argument("player", StringArgumentType.word())
											.suggests(Suggestions.PLAYERS)
											.executes(context -> obtain(context,
													StringArgumentType.getString(context, "player"))))))
					.then(Commands.literal("wear")
							.then(Commands.argument("id", StringArgumentType.word())
									.suggests(Suggestions.OWNED_TITLES)
									.executes(TitleCommands::wear)))
					.then(Commands.literal("clear")
							.executes(TitleCommands::clear)));
		});
	}

	/** {@code /titles}: shows the title that is currently worn. */
	private static int current(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		TitlesManager manager = TitlesManager.get();
		String id = manager.wornId(player.getUUID());

		if (id == null) {
			source.sendSuccess(() -> TitleTexts.component("none"), false);
			return 1;
		}

		TitleDefinition title = manager.get(id);
		Component label = title == null ? Component.literal(id) : TitleColors.render(title.text, title.color);
		source.sendSuccess(() -> TitleTexts.component("current", label), false);
		return 1;
	}

	private static int create(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String text = StringArgumentType.getString(context, "text");
		String color = StringArgumentType.getString(context, "color");
		String id = StringArgumentType.getString(context, "id");

		if (!ensureEnabled(source)) {
			return 0;
		}

		if (!TitleColors.isValid(color)) {
			source.sendFailure(TitleTexts.component("invalid_color", color));
			return 0;
		}

		TitleDefinition created = TitlesManager.get().create(text, color, id);

		if (created == null) {
			source.sendFailure(TitleTexts.component("create_failed", id));
			return 0;
		}

		source.sendSuccess(() -> TitleTexts.component("created", created.id), true);
		return 1;
	}

	private static int obtain(CommandContext<CommandSourceStack> context, String playerName) {
		CommandSourceStack source = context.getSource();

		if (!ensureEnabled(source)) {
			return 0;
		}

		TitleDefinition title = TitlesManager.get().get(StringArgumentType.getString(context, "id"));

		if (title == null) {
			source.sendFailure(TitleTexts.component("not_found", StringArgumentType.getString(context, "id")));
			return 0;
		}

		ServerPlayer target;

		if (playerName == null) {
			target = source.getPlayer();
		} else {
			target = source.getServer().getPlayerList().getPlayerByName(playerName);
		}

		if (target == null) {
			source.sendFailure(playerName == null
					? Component.translatable("oxycarbide.player_only")
					: TitleTexts.component("target_missing", playerName));
			return 0;
		}

		TitlesManager manager = TitlesManager.get();

		if (manager.owns(target.getUUID(), title.id)) {
			source.sendFailure(TitleTexts.component("already_owned"));
			return 0;
		}

		manager.grant(target.getUUID(), title.id);

		if (target == source.getPlayer()) {
			source.sendSuccess(() -> TitleTexts.component("obtained", title.id), false);
			return 1;
		}

		source.sendSuccess(() -> TitleTexts.component("granted", title.id, target.getScoreboardName()), true);
		target.sendSystemMessage(TitleTexts.component("obtained", title.id));
		return 1;
	}

	private static int wear(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (!ensureEnabled(source)) {
			return 0;
		}

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String id = StringArgumentType.getString(context, "id");
		TitleDefinition title = TitlesManager.get().get(id);

		if (title == null) {
			source.sendFailure(TitleTexts.component("not_found", id));
			return 0;
		}

		if (!TitlesManager.get().wear(player.getUUID(), title.id)) {
			source.sendFailure(TitleTexts.component("not_owned"));
			return 0;
		}

		TitlesManager.get().refreshNameTag(player);
		Component label = TitleColors.render(title.text, title.color);
		source.sendSuccess(() -> TitleTexts.component("worn", label), false);
		return 1;
	}

	private static int clear(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (!ensureEnabled(source)) {
			return 0;
		}

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		TitlesManager.get().clearWorn(player.getUUID());
		TitlesManager.get().refreshNameTag(player);
		source.sendSuccess(() -> TitleTexts.component("cleared"), false);
		return 1;
	}

	private static boolean ensureEnabled(CommandSourceStack source) {
		if (com.kaze943.oxycarbide.config.OxycarbideConfig.get().isTitlesEnabled()) {
			return true;
		}

		source.sendFailure(TitleTexts.component("disabled"));
		return false;
	}
}
