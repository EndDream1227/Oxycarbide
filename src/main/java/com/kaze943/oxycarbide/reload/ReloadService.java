package com.kaze943.oxycarbide.reload;

import com.kaze943.oxycarbide.claim.ClaimTexts;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.hat.HatTexts;
import com.kaze943.oxycarbide.home.HomeTexts;
import com.kaze943.oxycarbide.login.LoginTexts;
import com.kaze943.oxycarbide.market.MarketTexts;
import com.kaze943.oxycarbide.menu.MenuManager;
import com.kaze943.oxycarbide.menu.MenuTexts;
import com.kaze943.oxycarbide.mute.MuteTexts;
import com.kaze943.oxycarbide.network.ClientReloadPayload;
import com.kaze943.oxycarbide.nickname.NicknameTexts;
import com.kaze943.oxycarbide.prompt.PromptModes;
import com.kaze943.oxycarbide.shop.ShopManager;
import com.kaze943.oxycarbide.shop.ShopTexts;
import com.kaze943.oxycarbide.sidebar.CounterManager;
import com.kaze943.oxycarbide.sidebar.SidebarManager;
import com.kaze943.oxycarbide.sidebar.SidebarTexts;
import com.kaze943.oxycarbide.title.TitleTexts;
import com.kaze943.oxycarbide.title.TitlesManager;
import com.kaze943.oxycarbide.sweep.SweepTexts;
import com.kaze943.oxycarbide.text.JsonTexts;
import com.kaze943.oxycarbide.tpa.TpaTexts;
import com.kaze943.oxycarbide.travel.BackTexts;
import com.kaze943.oxycarbide.travel.RtpTexts;
import com.kaze943.oxycarbide.travel.SpawnTexts;
import com.kaze943.oxycarbide.treasury.SpecialTreasuryTexts;
import com.kaze943.oxycarbide.treasury.TreasuryTexts;
import com.kaze943.oxycarbide.warp.WarpTexts;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Backs {@code /oxycarbide reload server} and {@code /oxycarbide reload client}.
 *
 * <p>The server variant re-reads everything that lives in {@code config/}: the settings
 * file, the editable texts of every language and the shop files. A running server never
 * needs a restart after editing those.</p>
 *
 * <p>The client variant asks one client to reload its resource packs, exactly like
 * {@code F3 + T}. That is the useful one when a local (single player) server left the
 * client with stale language files or textures.</p>
 */
public final class ReloadService {
	private ReloadService() {
	}

	/**
	 * Reloads the server side and returns the number of text files that were read.
	 */
	public static int reloadServer(MinecraftServer server) {
		OxycarbideConfig.load();
		JsonTexts.beginBatch();
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
		MenuTexts.load();
		MarketTexts.load();
		SweepTexts.load();
		ShopManager.get().load();
		MenuManager.get().load();
		TitleTexts.load();
		TitlesManager.get().load(server);
		SidebarTexts.load();
		SidebarManager.get().load();
		CounterManager.get().load(server);
		PromptModes.get().load(server);
		return JsonTexts.batchFiles();
	}

	/**
	 * @return {@code false} when that client does not have oxycarbide installed, so the
	 *         command can explain why nothing happened
	 */
	public static boolean reloadClient(ServerPlayer player) {
		if (!ServerPlayNetworking.canSend(player, ClientReloadPayload.TYPE)) {
			return false;
		}

		ServerPlayNetworking.send(player, new ClientReloadPayload());
		return true;
	}
}
