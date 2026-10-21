package com.kaze943.oxycarbide;

import com.kaze943.oxycarbide.command.ClaimCommands;
import com.kaze943.oxycarbide.command.ConfigCommands;
import com.kaze943.oxycarbide.command.HatCommands;
import com.kaze943.oxycarbide.command.HomeCommands;
import com.kaze943.oxycarbide.command.InvSeeCommand;
import com.kaze943.oxycarbide.command.KickMeCommands;
import com.kaze943.oxycarbide.command.LoginAdminCommands;
import com.kaze943.oxycarbide.command.LoginCommand;
import com.kaze943.oxycarbide.command.MarketCommands;
import com.kaze943.oxycarbide.command.MuteCommands;
import com.kaze943.oxycarbide.command.NicknameCommand;
import com.kaze943.oxycarbide.command.ShopCommands;
import com.kaze943.oxycarbide.command.SpecialTreasuryCommands;
import com.kaze943.oxycarbide.command.SweepCommands;
import com.kaze943.oxycarbide.command.TeleportLastCommand;
import com.kaze943.oxycarbide.command.TpaCommands;
import com.kaze943.oxycarbide.command.TrashcanCommand;
import com.kaze943.oxycarbide.command.TravelCommands;
import com.kaze943.oxycarbide.command.TreasuryCommands;
import com.kaze943.oxycarbide.command.WarpCommands;
import com.kaze943.oxycarbide.claim.ClaimManager;
import com.kaze943.oxycarbide.claim.ClaimProtection;
import com.kaze943.oxycarbide.claim.ClaimTexts;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.hat.HatTexts;
import com.kaze943.oxycarbide.home.HomeManager;
import com.kaze943.oxycarbide.home.HomeTexts;
import com.kaze943.oxycarbide.market.MarketManager;
import com.kaze943.oxycarbide.market.MarketService;
import com.kaze943.oxycarbide.market.MarketTexts;
import com.kaze943.oxycarbide.sweep.SweepManager;
import com.kaze943.oxycarbide.sweep.SweepTexts;
import com.kaze943.oxycarbide.login.LoginManager;
import com.kaze943.oxycarbide.login.LoginProtection;
import com.kaze943.oxycarbide.login.LoginTexts;
import com.kaze943.oxycarbide.mute.MuteManager;
import com.kaze943.oxycarbide.mute.MuteTexts;
import com.kaze943.oxycarbide.nickname.NicknameManager;
import com.kaze943.oxycarbide.nickname.NicknameTexts;
import com.kaze943.oxycarbide.prompt.PromptModes;
import com.kaze943.oxycarbide.tpa.TpaManager;
import com.kaze943.oxycarbide.tpa.TpaTexts;
import com.kaze943.oxycarbide.tracking.LastPlayerTracker;
import com.kaze943.oxycarbide.travel.BackPoints;
import com.kaze943.oxycarbide.travel.BackTexts;
import com.kaze943.oxycarbide.travel.RtpManager;
import com.kaze943.oxycarbide.travel.RtpTexts;
import com.kaze943.oxycarbide.travel.SpawnTexts;
import com.kaze943.oxycarbide.treasury.SpecialCurrencyManager;
import com.kaze943.oxycarbide.treasury.SpecialTreasuryTexts;
import com.kaze943.oxycarbide.treasury.TreasuryManager;
import com.kaze943.oxycarbide.treasury.TreasuryTexts;
import com.kaze943.oxycarbide.shop.ShopManager;
import com.kaze943.oxycarbide.shop.ShopTexts;
import com.kaze943.oxycarbide.warp.WarpManager;
import com.kaze943.oxycarbide.warp.WarpTexts;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oxycarbide implements ModInitializer {
	public static final String MOD_ID = "oxycarbide";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Settings first: the command handlers read the limits and rules at execution time.
		OxycarbideConfig.load();

		HomeCommands.register();
		WarpCommands.register();
		TravelCommands.register();
		TpaCommands.register();
		KickMeCommands.register();
		NicknameCommand.register();
		MuteCommands.register();
		InvSeeCommand.register();
		TeleportLastCommand.register();
		TrashcanCommand.register();
		TreasuryCommands.register();
		SpecialTreasuryCommands.register();
		ShopCommands.register();
		MarketCommands.register();
		SweepCommands.register();
		ClaimCommands.register();
		LoginCommand.register();
		LoginAdminCommands.register();
		HatCommands.register();
		ConfigCommands.register();

		LoginProtection.register();
		ClaimProtection.register();

		// A death is one of the two things /back may return to.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayer player) {
				BackPoints.remember(player, (ServerLevel) player.level(),
						player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			}
		});

		// /rtp generates at most one chunk per tick, so it is driven from the server tick.
		ServerTickEvents.END_SERVER_TICK.register(RtpManager.get()::tick);
		// TPA requests expire and their accepted teleports count down on the same tick.
		ServerTickEvents.END_SERVER_TICK.register(TpaManager.get()::tick);

		// Remember where the most recently seen player was, for /telepoint_last.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			LastPlayerTracker.get().update(handler.getPlayer());
			LoginManager.get().onJoin(handler.getPlayer(), server);
			// Register a zero-balance account so /listreasury lists every player.
			TreasuryManager.get().ensureAccount(handler.getPlayer().getUUID(), handler.getPlayer().getScoreboardName());
			// Items refunded while the player was offline are handed back on join.
			MarketService.get().deliverPending(handler.getPlayer());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			LastPlayerTracker.get().update(handler.getPlayer());
			LoginManager.get().onDisconnect(handler.getPlayer());
		});

		// Login sessions are frozen and timed out on the server tick.
		ServerTickEvents.END_SERVER_TICK.register(LoginManager.get()::tick);
		// "Grounded" claims push non-allowed players back out of the chunk.
		ServerTickEvents.END_SERVER_TICK.register(ClaimManager.get()::tick);
		// Automatic and smart sweeps are driven from the server tick.
		ServerTickEvents.END_SERVER_TICK.register(SweepManager.get()::tick);

		// Homes and warps live in the world save, so they are loaded once the world is
		// ready and written back when the server stops.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			HomeManager.get().load(server);
			WarpManager.get().load(server);
			NicknameManager.get().load(server);
			MuteManager.get().load(server);
			LastPlayerTracker.get().load(server);
			LoginManager.get().load(server);
			LoginManager.get().markStarted();
			TreasuryManager.get().load(server);
			SpecialCurrencyManager.get().load(server);
			ClaimManager.get().load(server);
			ShopManager.get().load();
			MarketManager.get().load(server);
			PromptModes.get().load(server);
			HomeTexts.load();
			WarpTexts.load();
			HatTexts.load();
			NicknameTexts.load();
			LoginTexts.load();
			TreasuryTexts.load();
			SpecialTreasuryTexts.load();
			MuteTexts.load();
			TpaTexts.load();
			SpawnTexts.load();
			BackTexts.load();
			RtpTexts.load();
			ClaimTexts.load();
			ShopTexts.load();
			MarketTexts.load();
			SweepTexts.load();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			HomeManager.get().save();
			WarpManager.get().save();
			NicknameManager.get().save();
			MuteManager.get().save();
			LastPlayerTracker.get().save();
			LoginManager.get().save();
			TreasuryManager.get().save();
			SpecialCurrencyManager.get().save();
			ClaimManager.get().save();
			MarketManager.get().save();
			PromptModes.get().save();
			BackPoints.clear();
			RtpManager.get().clear();
			TpaManager.get().clear();
			LoginManager.get().clear();
			SpecialCurrencyManager.get().clear();
			ClaimManager.get().clear();
			PromptModes.get().clear();
		});

		LOGGER.info("Oxycarbide loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
