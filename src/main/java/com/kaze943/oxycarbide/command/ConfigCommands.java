package com.kaze943.oxycarbide.command;

import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.prompt.PromptModes;
import com.kaze943.oxycarbide.util.Admins;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /oxycarbide config ...} and {@code /oxycarbide rule ...}, the in-game equivalent
 * of {@code config/oxycarbide.json}.
 */
public final class ConfigCommands {
	private static final String RULE_CREATE = "OnlyOpCreateWarp";
	private static final String RULE_EDIT = "OnlyOpEditWarp";
	private static final String RULE_DELETE = "OnlyOpDeleteWarp";
	private static final String RULE_PROTECT = "rtpAutoProtect";
	private static final String RULE_TRANSFER = "allowPlayerTransTreasury";
	private static final String RULE_SPECIAL = "allowSpecialCurrency";
	private static final String RULE_SP_TRANSFER = "allowPlayerTranSPTreasury";
	private static final String RULE_CLAIM = "OnlyOpPlayerClaim";
	private static final String RULE_SILENT_SWEEP = "slienceSweep";

	private ConfigCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("oxycarbide")
						.then(Commands.literal("config")
								.then(homeNode())
								.then(warpNode())
								.then(spawnNode())
								.then(backNode())
								.then(rtpNode())
								.then(tpaNode())
								.then(kickMeNode())
								.then(nicknameNode())
								.then(muteNode())
								.then(trashcanNode())
								.then(loginNode())
								.then(treasuryNode())
								.then(claimNode())
								.then(shopNode())
								.then(marketNode())
								.then(sweepNode())
								.then(smartSweepNode())
								.then(promptModeNode()))
						.then(Commands.literal("rule")
								.executes(ConfigCommands::ruleStatus)
								.then(ruleNode(RULE_CREATE))
								.then(ruleNode(RULE_EDIT))
								.then(ruleNode(RULE_DELETE))
								.then(ruleNode(RULE_PROTECT))
								.then(ruleNode(RULE_TRANSFER))
								.then(ruleNode(RULE_SPECIAL))
								.then(ruleNode(RULE_SP_TRANSFER))
								.then(ruleNode(RULE_CLAIM))
								.then(ruleNode(RULE_SILENT_SWEEP)))));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> promptModeNode() {
		return Commands.literal("promptmode")
				.executes(ConfigCommands::promptModeStatus)
				.then(Commands.literal(PromptModes.DIALOG)
						.executes(context -> setPromptMode(context, PromptModes.DIALOG)))
				.then(Commands.literal(PromptModes.TITLE)
						.executes(context -> setPromptMode(context, PromptModes.TITLE)));
	}

	private static int promptModeStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		String mode = PromptModes.get().mode(player.getUUID());
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.promptmode.status",
				Component.translatable(PromptModes.TITLE.equals(mode)
						? "oxycarbide.config.promptmode.name_title"
						: "oxycarbide.config.promptmode.name_dialog")), false);
		return 1;
	}

	private static int setPromptMode(CommandContext<CommandSourceStack> context, String mode) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			source.sendFailure(Component.translatable("oxycarbide.player_only"));
			return 0;
		}

		PromptModes.get().set(player.getUUID(), mode);
		source.sendSuccess(() -> Component.translatable(PromptModes.TITLE.equals(mode)
				? "oxycarbide.config.promptmode.set_title"
				: "oxycarbide.config.promptmode.set_dialog"), false);
		return 1;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> homeNode() {
		return Commands.literal("home")
				.executes(ConfigCommands::homeStatus)
				.then(Commands.literal("number")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setHomeLimit)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setHomeSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> warpNode() {
		return Commands.literal("warp")
				.executes(ConfigCommands::warpStatus)
				.then(Commands.literal("number")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setWarpLimit)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setWarpSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> spawnNode() {
		return Commands.literal("spawn")
				.executes(ConfigCommands::spawnStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setSpawnSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> backNode() {
		return Commands.literal("back")
				.executes(ConfigCommands::backStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setBackSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> rtpNode() {
		return Commands.literal("rtp")
				.executes(ConfigCommands::rtpStatus)
				.then(Commands.literal("radius")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setRtpRadius)))
				.then(Commands.literal("countdown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setRtpCountdown)))
				.then(Commands.literal("cooldown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setRtpCooldown)))
				.then(Commands.literal("autoprotectnum")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setRtpAutoProtectNum)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setRtpSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> tpaNode() {
		return Commands.literal("tpa")
				.executes(ConfigCommands::tpaStatus)
				.then(Commands.literal("countdown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setTpaCountdown)))
				.then(Commands.literal("cooldown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setTpaCooldown)))
				.then(Commands.literal("autodeny")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setTpaAutoDeny)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setTpaSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> kickMeNode() {
		return Commands.literal("kickme")
				.executes(ConfigCommands::kickMeStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setKickMeSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> nicknameNode() {
		return Commands.literal("nickname")
				.executes(ConfigCommands::nicknameStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setNicknameSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> muteNode() {
		return Commands.literal("mute")
				.executes(ConfigCommands::muteStatus)
				.then(Commands.literal("timer")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setMuteTimer)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setMuteSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> trashcanNode() {
		return Commands.literal("trashcan")
				.executes(ConfigCommands::trashcanStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setTrashcanSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> loginNode() {
		return Commands.literal("login")
				.executes(ConfigCommands::loginStatus)
				.then(Commands.literal("mode")
						.requires(Admins::isAdmin)
						.then(Commands.literal("normal")
								.executes(context -> setLoginMode(context, false)))
						.then(Commands.literal("simple")
								.executes(context -> setLoginMode(context, true))))
				.then(Commands.literal("time_first")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setLoginTimeFirst)))
				.then(Commands.literal("time")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setLoginTime)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setLoginSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> treasuryNode() {
		return Commands.literal("treasury")
				.executes(ConfigCommands::treasuryStatus)
				.then(Commands.literal("translimit")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", LongArgumentType.longArg(0))
								.executes(ConfigCommands::setTreasuryLimit)))
				.then(Commands.literal("currencyname")
						.requires(Admins::isAdmin)
						.then(Commands.argument("name", StringArgumentType.word())
								.executes(ConfigCommands::setCurrencyName)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setTreasurySwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> claimNode() {
		return Commands.literal("claim")
				.executes(ConfigCommands::claimStatus)
				.then(Commands.literal("value")
						.requires(Admins::isAdmin)
						.then(Commands.argument("currency", StringArgumentType.word())
								.then(Commands.argument("amount", LongArgumentType.longArg(0))
										.executes(ConfigCommands::setClaimValue))))
				.then(Commands.literal("maxnum")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setClaimMaxNum)))
				.then(Commands.literal("forceloadMaxnum")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setClaimForceloadMaxNum)))
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setClaimSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> shopNode() {
		return Commands.literal("shop")
				.executes(ConfigCommands::shopStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setShopSwitch)))
				.then(Commands.literal("default_time")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setMarketDefaultTime)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> marketNode() {
		return Commands.literal("market")
				.executes(ConfigCommands::marketStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setMarketSwitch)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> sweepNode() {
		return Commands.literal("sweep")
				.executes(ConfigCommands::sweepStatus)
				.then(Commands.literal("switch")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setSweepSwitch)))
				.then(Commands.literal("time")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(ConfigCommands::setSweepTime)))
				.then(Commands.literal("countdown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", StringArgumentType.string())
								.executes(ConfigCommands::setSweepCountdown)))
				.then(Commands.literal("mode")
						.requires(Admins::isAdmin)
						.then(Commands.literal("simple")
								.executes(context -> setSweepMode(context, "simple")))
						.then(Commands.literal("entity")
								.executes(context -> setSweepMode(context, "entity"))))
				.then(Commands.literal("whitelist")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", StringArgumentType.word())
								.executes(ConfigCommands::addSweepWhitelist)))
				.then(Commands.literal("delwhitelist")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", StringArgumentType.word())
								.executes(ConfigCommands::removeSweepWhitelist)))
				.then(Commands.literal("smartsweep")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ConfigCommands::setSmartSweep)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> smartSweepNode() {
		return Commands.literal("smartsweep")
				.executes(ConfigCommands::smartSweepStatus)
				.then(Commands.literal("threshold")
						.requires(Admins::isAdmin)
						.then(Commands.argument("tps", DoubleArgumentType.doubleArg(0.0, 20.0))
								.then(Commands.argument("drops", IntegerArgumentType.integer(0))
										.executes(ConfigCommands::setSmartThreshold))))
				.then(Commands.literal("scancountdown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setScanCooldown)))
				.then(Commands.literal("sweepcountdown")
						.requires(Admins::isAdmin)
						.then(Commands.argument("value", IntegerArgumentType.integer(1))
								.executes(ConfigCommands::setSweepCooldown)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> ruleNode(String rule) {
		return Commands.literal(rule)
				.requires(Admins::isAdmin)
				.then(Commands.argument("value", BoolArgumentType.bool())
						.executes(context -> setRule(context, rule)));
	}

	private static int shopStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.shop.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isShopEnabled()
				? "oxycarbide.config.shop.status_enabled"
				: "oxycarbide.config.shop.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.shop.default_time_status",
				config.getMarketDefaultMinutes()), false);
		return 1;
	}

	private static int setShopSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setShopEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.shop.switch_on"
				: "oxycarbide.config.shop.switch_off"), true);
		return 1;
	}

	private static int setMarketDefaultTime(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setMarketDefaultMinutes(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.shop.default_time_set", value), true);
		return 1;
	}

	private static int marketStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.market.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isMarketEnabled()
				? "oxycarbide.config.market.status_enabled"
				: "oxycarbide.config.market.status_disabled"), false);
		return 1;
	}

	private static int setMarketSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setMarketEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.market.switch_on"
				: "oxycarbide.config.market.switch_off"), true);
		return 1;
	}

	private static int sweepStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isSweepEnabled()
				? "oxycarbide.config.sweep.status_enabled"
				: "oxycarbide.config.sweep.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.time_status",
				config.getSweepTimeMinutes()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.countdown_status",
				config.getSweepCountdownSeconds()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.mode_status",
				config.getSweepMode()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.whitelist_status",
				config.getSweepWhitelist().size()), false);
		source.sendSuccess(() -> Component.translatable(config.isSmartSweep()
				? "oxycarbide.config.sweep.smart_on"
				: "oxycarbide.config.sweep.smart_off"), false);
		return 1;
	}

	private static int setSweepSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setSweepEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.sweep.switch_on"
				: "oxycarbide.config.sweep.switch_off"), true);
		return 1;
	}

	private static int setSweepTime(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setSweepTimeMinutes(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.time_set", value), true);
		return 1;
	}

	private static int setSweepCountdown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String value = StringArgumentType.getString(context, "value");

		OxycarbideConfig.get().setSweepCountdownSeconds(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.countdown_set", value), true);
		return 1;
	}

	private static int setSweepMode(CommandContext<CommandSourceStack> context, String mode) {
		CommandSourceStack source = context.getSource();

		OxycarbideConfig.get().setSweepMode(mode);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.mode_set", mode), true);
		return 1;
	}

	private static int addSweepWhitelist(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String id = StringArgumentType.getString(context, "value");

		if (!OxycarbideConfig.get().addSweepWhitelist(id)) {
			source.sendFailure(Component.translatable("oxycarbide.config.sweep.whitelist_exists", id));
			return 0;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.whitelist_added", id), true);
		return 1;
	}

	private static int removeSweepWhitelist(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String id = StringArgumentType.getString(context, "value");

		if (!OxycarbideConfig.get().removeSweepWhitelist(id)) {
			source.sendFailure(Component.translatable("oxycarbide.config.sweep.whitelist_missing", id));
			return 0;
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.sweep.whitelist_removed", id), true);
		return 1;
	}

	private static int setSmartSweep(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean value = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setSmartSweep(value);
		source.sendSuccess(() -> Component.translatable(value
				? "oxycarbide.config.sweep.smart_switch_on"
				: "oxycarbide.config.sweep.smart_switch_off"), true);
		return 1;
	}

	private static int smartSweepStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isSmartSweep()
				? "oxycarbide.config.sweep.smart_on"
				: "oxycarbide.config.sweep.smart_off"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.threshold_status",
				config.getSmartTpsThreshold(), config.getSmartDropThreshold()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.scan_status",
				config.getSmartScanCooldownSeconds()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.sweep_status",
				config.getSmartSweepCooldownSeconds()), false);
		return 1;
	}

	private static int setSmartThreshold(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		double tps = DoubleArgumentType.getDouble(context, "tps");
		int drops = IntegerArgumentType.getInteger(context, "drops");

		OxycarbideConfig.get().setSmartTpsThreshold(tps);
		OxycarbideConfig.get().setSmartDropThreshold(drops);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.threshold_set",
				tps, drops), true);
		return 1;
	}

	private static int setScanCooldown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setSmartScanCooldownSeconds(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.scan_set", value), true);
		return 1;
	}

	private static int setSweepCooldown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setSmartSweepCooldownSeconds(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.smartsweep.sweep_set", value), true);
		return 1;
	}

	private static int homeStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.home.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isHomeEnabled()
				? "oxycarbide.config.home.status_enabled"
				: "oxycarbide.config.home.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.home.limit_status", config.getHomeLimit()), false);
		return 1;
	}

	private static int setHomeLimit(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setHomeLimit(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.home.limit_set", value), true);
		return 1;
	}

	private static int setHomeSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setHomeEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.home.switch_on"
				: "oxycarbide.config.home.switch_off"), true);
		return 1;
	}

	private static int warpStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.warp.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isWarpEnabled()
				? "oxycarbide.config.warp.status_enabled"
				: "oxycarbide.config.warp.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.warp.limit_status", config.getWarpLimit()), false);
		return 1;
	}

	private static int setWarpLimit(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setWarpLimit(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.warp.limit_set", value), true);
		return 1;
	}

	private static int setWarpSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setWarpEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.warp.switch_on"
				: "oxycarbide.config.warp.switch_off"), true);
		return 1;
	}

	private static int spawnStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.spawn.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isSpawnEnabled()
				? "oxycarbide.config.spawn.status_enabled"
				: "oxycarbide.config.spawn.status_disabled"), false);
		return 1;
	}

	private static int setSpawnSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setSpawnEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.spawn.switch_on"
				: "oxycarbide.config.spawn.switch_off"), true);
		return 1;
	}

	private static int backStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.back.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isBackEnabled()
				? "oxycarbide.config.back.status_enabled"
				: "oxycarbide.config.back.status_disabled"), false);
		return 1;
	}

	private static int setBackSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setBackEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.back.switch_on"
				: "oxycarbide.config.back.switch_off"), true);
		return 1;
	}

	private static int rtpStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isRtpEnabled()
				? "oxycarbide.config.rtp.status_enabled"
				: "oxycarbide.config.rtp.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.radius_status", config.getRtpRadius()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.countdown_status", config.getRtpCountdown()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.cooldown_status", config.getRtpCooldown()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.autoprotectnum_status", config.getRtpAutoProtectNum()), false);
		return 1;
	}

	private static int setRtpRadius(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setRtpRadius(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.radius_set", value), true);
		return 1;
	}

	private static int setRtpCountdown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setRtpCountdown(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.countdown_set", value), true);
		return 1;
	}

	private static int setRtpCooldown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setRtpCooldown(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.cooldown_set", value), true);
		return 1;
	}

	private static int setRtpAutoProtectNum(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setRtpAutoProtectNum(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rtp.autoprotectnum_set", value), true);
		return 1;
	}

	private static int setRtpSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setRtpEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.rtp.switch_on"
				: "oxycarbide.config.rtp.switch_off"), true);
		return 1;
	}

	private static int tpaStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isTpaEnabled()
				? "oxycarbide.config.tpa.status_enabled"
				: "oxycarbide.config.tpa.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.countdown_status", config.getTpaCountdown()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.cooldown_status", config.getTpaCooldown()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.autodeny_status", config.getTpaAutoDeny()), false);
		return 1;
	}

	private static int setTpaCountdown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setTpaCountdown(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.countdown_set", value), true);
		return 1;
	}

	private static int setTpaCooldown(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setTpaCooldown(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.cooldown_set", value), true);
		return 1;
	}

	private static int setTpaAutoDeny(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setTpaAutoDeny(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.tpa.autodeny_set", value), true);
		return 1;
	}

	private static int setTpaSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setTpaEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.tpa.switch_on"
				: "oxycarbide.config.tpa.switch_off"), true);
		return 1;
	}

	private static int kickMeStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.kickme.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isKickMeEnabled()
				? "oxycarbide.config.kickme.status_enabled"
				: "oxycarbide.config.kickme.status_disabled"), false);
		return 1;
	}

	private static int setKickMeSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setKickMeEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.kickme.switch_on"
				: "oxycarbide.config.kickme.switch_off"), true);
		return 1;
	}

	private static int nicknameStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.nickname.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isNicknameEnabled()
				? "oxycarbide.config.nickname.status_enabled"
				: "oxycarbide.config.nickname.status_disabled"), false);
		return 1;
	}

	private static int setNicknameSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setNicknameEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.nickname.switch_on"
				: "oxycarbide.config.nickname.switch_off"), true);
		return 1;
	}

	private static int muteStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.mute.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isMuteEnabled()
				? "oxycarbide.config.mute.status_enabled"
				: "oxycarbide.config.mute.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.mute.timer_status", config.getMuteTimer()), false);
		return 1;
	}

	private static int setMuteTimer(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setMuteTimer(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.mute.timer_set", value), true);
		return 1;
	}

	private static int setMuteSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setMuteEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.mute.switch_on"
				: "oxycarbide.config.mute.switch_off"), true);
		return 1;
	}

	private static int trashcanStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.trashcan.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isTrashcanEnabled()
				? "oxycarbide.config.trashcan.status_enabled"
				: "oxycarbide.config.trashcan.status_disabled"), false);
		return 1;
	}

	private static int setTrashcanSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setTrashcanEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.trashcan.switch_on"
				: "oxycarbide.config.trashcan.switch_off"), true);
		return 1;
	}

	private static int loginStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isLoginEnabled()
				? "oxycarbide.config.login.status_enabled"
				: "oxycarbide.config.login.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.mode_status", loginModeOf(config)), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.time_first_status", config.getLoginTimeFirst()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.time_status", config.getLoginTime()), false);
		return 1;
	}

	private static int setLoginMode(CommandContext<CommandSourceStack> context, boolean simple) {
		CommandSourceStack source = context.getSource();

		OxycarbideConfig.get().setLoginSimpleMode(simple);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.mode_set", loginModeOf(OxycarbideConfig.get())), true);
		return 1;
	}

	private static int setLoginTimeFirst(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setLoginTimeFirst(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.time_first_set", value), true);
		return 1;
	}

	private static int setLoginTime(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setLoginTime(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.login.time_set", value), true);
		return 1;
	}

	private static int setLoginSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setLoginEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.login.switch_on"
				: "oxycarbide.config.login.switch_off"), true);
		return 1;
	}

	private static Component loginModeOf(OxycarbideConfig config) {
		return Component.translatable(config.isLoginSimpleMode()
				? "oxycarbide.config.login.mode_simple"
				: "oxycarbide.config.login.mode_normal");
	}

	private static int treasuryStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.treasury.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isTreasuryEnabled()
				? "oxycarbide.config.treasury.status_enabled"
				: "oxycarbide.config.treasury.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.treasury.limit_status",
				config.getTreasuryTransferLimit()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.treasury.currencyname_status",
				config.getCurrencyName()), false);
		return 1;
	}

	private static int setCurrencyName(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String name = StringArgumentType.getString(context, "name");

		OxycarbideConfig.get().setCurrencyName(name);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.treasury.currencyname_set", name), true);
		return 1;
	}

	private static int claimStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.header"), false);
		source.sendSuccess(() -> Component.translatable(config.isClaimEnabled()
				? "oxycarbide.config.claim.status_enabled"
				: "oxycarbide.config.claim.status_disabled"), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.value_status",
				config.getClaimCostCurrency(), config.getClaimCostAmount()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.maxnum_status",
				config.getClaimMaxNum()), false);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.forceload_status",
				config.getClaimForceloadMaxNum()), false);
		return 1;
	}

	private static int setClaimValue(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String currency = StringArgumentType.getString(context, "currency");
		long amount = LongArgumentType.getLong(context, "amount");

		OxycarbideConfig.get().setClaimCostCurrency(currency);
		OxycarbideConfig.get().setClaimCostAmount(amount);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.value_set",
				currency, amount), true);
		return 1;
	}

	private static int setClaimMaxNum(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setClaimMaxNum(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.maxnum_set", value), true);
		return 1;
	}

	private static int setClaimForceloadMaxNum(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		int value = IntegerArgumentType.getInteger(context, "value");

		OxycarbideConfig.get().setClaimForceloadMaxNum(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.claim.forceload_set", value), true);
		return 1;
	}

	private static int setClaimSwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setClaimEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.claim.switch_on"
				: "oxycarbide.config.claim.switch_off"), true);
		return 1;
	}

	private static int setTreasuryLimit(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		long value = LongArgumentType.getLong(context, "value");

		OxycarbideConfig.get().setTreasuryTransferLimit(value);
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.treasury.limit_set", value), true);
		return 1;
	}

	private static int setTreasurySwitch(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		boolean enabled = BoolArgumentType.getBool(context, "value");

		OxycarbideConfig.get().setTreasuryEnabled(enabled);
		source.sendSuccess(() -> Component.translatable(enabled
				? "oxycarbide.config.treasury.switch_on"
				: "oxycarbide.config.treasury.switch_off"), true);
		return 1;
	}

	private static int ruleStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		OxycarbideConfig config = OxycarbideConfig.get();

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rule.header"), false);
		sendRule(source, RULE_CREATE, config.isOnlyOpCreateWarp());
		sendRule(source, RULE_EDIT, config.isOnlyOpEditWarp());
		sendRule(source, RULE_DELETE, config.isOnlyOpDeleteWarp());
		sendRule(source, RULE_PROTECT, config.isRtpAutoProtect());
		sendRule(source, RULE_TRANSFER, config.isAllowPlayerTransTreasury());
		sendRule(source, RULE_SPECIAL, config.isAllowSpecialCurrency());
		sendRule(source, RULE_SP_TRANSFER, config.isAllowPlayerTranSPTreasury());
		sendRule(source, RULE_CLAIM, config.isOnlyOpPlayerClaim());
		sendRule(source, RULE_SILENT_SWEEP, config.isSilentSweep());
		return 1;
	}

	private static int setRule(CommandContext<CommandSourceStack> context, String rule) {
		CommandSourceStack source = context.getSource();
		boolean value = BoolArgumentType.getBool(context, "value");
		OxycarbideConfig config = OxycarbideConfig.get();

		switch (rule) {
			case RULE_CREATE -> config.setOnlyOpCreateWarp(value);
			case RULE_EDIT -> config.setOnlyOpEditWarp(value);
			case RULE_DELETE -> config.setOnlyOpDeleteWarp(value);
			case RULE_PROTECT -> config.setRtpAutoProtect(value);
			case RULE_TRANSFER -> config.setAllowPlayerTransTreasury(value);
			case RULE_SPECIAL -> config.setAllowSpecialCurrency(value);
			case RULE_CLAIM -> config.setOnlyOpPlayerClaim(value);
			case RULE_SILENT_SWEEP -> config.setSilentSweep(value);
			case RULE_SP_TRANSFER -> config.setAllowPlayerTranSPTreasury(value);
			default -> {
				return 0;
			}
		}

		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rule.set", rule, stateOf(value)), true);

		if (RULE_PROTECT.equals(rule) && !value) {
			source.sendSuccess(() -> Component.translatable("oxycarbide.config.rule.rtp_protect_off"), false);
		}

		return 1;
	}

	private static void sendRule(CommandSourceStack source, String rule, boolean value) {
		source.sendSuccess(() -> Component.translatable("oxycarbide.config.rule.line", rule, stateOf(value)), false);
	}

	private static Component stateOf(boolean value) {
		return Component.translatable(value ? "oxycarbide.value.on" : "oxycarbide.value.off");
	}
}
