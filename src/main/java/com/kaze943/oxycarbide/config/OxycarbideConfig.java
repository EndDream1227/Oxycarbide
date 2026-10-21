package com.kaze943.oxycarbide.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kaze943.oxycarbide.Oxycarbide;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mod settings, stored in {@code config/oxycarbide.json}.
 */
public final class OxycarbideConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "oxycarbide.json";

	private static OxycarbideConfig instance = new OxycarbideConfig();

	private boolean homeEnabled = true;
	private int homeLimit = 0;

	private boolean warpEnabled = true;
	private int warpLimit = 1;
	private boolean onlyOpCreateWarp = true;
	private boolean onlyOpEditWarp = true;
	private boolean onlyOpDeleteWarp = true;

	private boolean spawnEnabled = true;
	private boolean backEnabled = true;
	private boolean rtpEnabled = true;
	private int rtpRadius = 1000;
	private int rtpCountdown = 100;
	private int rtpCooldown = 600;
	private boolean rtpAutoProtect = true;
	private int rtpAutoProtectNum = 3;

	private boolean tpaEnabled = true;
	private int tpaCountdown = 60;
	private int tpaCooldown = 100;
	private int tpaAutoDeny = 100;

	private boolean kickMeEnabled = true;
	private boolean nicknameEnabled = true;
	private boolean muteEnabled = true;
	private int muteTimer = 1200;
	private boolean trashcanEnabled = true;

	private boolean loginEnabled = false;
	private boolean loginSimpleMode = false;
	private int loginTimeFirst = 1200;
	private int loginTime = 600;

	private boolean treasuryEnabled = false;
	private long treasuryTransferLimit = 0L;
	private boolean allowPlayerTransTreasury = true;
	private boolean allowSpecialCurrency = false;
	private boolean allowPlayerTranSPTreasury = true;

	private String currencyName = "金币";
	private boolean claimEnabled = false;
	private String claimCostCurrency = "normal";
	private long claimCostAmount = 0L;
	private int claimMaxNum = 100;
	private int claimForceloadMaxNum = 20;
	private boolean onlyOpPlayerClaim = true;
	private String shopDefaultId = "";
	private boolean shopEnabled = false;
	private boolean marketEnabled = false;
	private int marketDefaultMinutes = 180;

	private boolean sweepEnabled = true;
	private int sweepTimeMinutes = 5;
	private String sweepCountdownSeconds = "120,60,30,15,5,4,3,2,1";
	private boolean silentSweep = false;
	private String sweepMode = "simple";
	private java.util.List<String> sweepWhitelist = new java.util.ArrayList<>();
	private boolean smartSweep = false;
	private double smartTpsThreshold = 15.0;
	private int smartDropThreshold = 200;
	private int smartScanCooldownSeconds = 15;
	private int smartSweepCooldownSeconds = 10;

	private OxycarbideConfig() {
	}

	public static OxycarbideConfig get() {
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static void load() {
		OxycarbideConfig loaded = new OxycarbideConfig();
		Path path = path();

		if (Files.exists(path)) {
			try {
				OxycarbideConfig fromDisk = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), OxycarbideConfig.class);

				if (fromDisk != null) {
					loaded = fromDisk;
				}
			} catch (IOException | JsonSyntaxException e) {
				Oxycarbide.LOGGER.warn("Failed to read config from {}, using defaults", path, e);
			}
		}

		if (loaded.homeLimit < 0) {
			loaded.homeLimit = 0;
		}

		instance = loaded;
		instance.save();
	}

	public void save() {
		Path path = path();

		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Oxycarbide.LOGGER.warn("Failed to write config to {}", path, e);
		}
	}

	public boolean isHomeEnabled() {
		return homeEnabled;
	}

	public void setHomeEnabled(boolean enabled) {
		this.homeEnabled = enabled;
		save();
	}

	/** {@code 0} means "unlimited". */
	public int getHomeLimit() {
		return homeLimit;
	}

	public void setHomeLimit(int limit) {
		this.homeLimit = Math.max(0, limit);
		save();
	}

	public boolean isWarpEnabled() {
		return warpEnabled;
	}

	public void setWarpEnabled(boolean enabled) {
		this.warpEnabled = enabled;
		save();
	}

	/** {@code 0} means "unlimited". */
	public int getWarpLimit() {
		return warpLimit;
	}

	public void setWarpLimit(int limit) {
		this.warpLimit = Math.max(0, limit);
		save();
	}

	public boolean isOnlyOpCreateWarp() {
		return onlyOpCreateWarp;
	}

	public void setOnlyOpCreateWarp(boolean value) {
		this.onlyOpCreateWarp = value;
		save();
	}

	public boolean isOnlyOpEditWarp() {
		return onlyOpEditWarp;
	}

	public void setOnlyOpEditWarp(boolean value) {
		this.onlyOpEditWarp = value;
		save();
	}

	public boolean isOnlyOpDeleteWarp() {
		return onlyOpDeleteWarp;
	}

	public void setOnlyOpDeleteWarp(boolean value) {
		this.onlyOpDeleteWarp = value;
		save();
	}

	public boolean isSpawnEnabled() {
		return spawnEnabled;
	}

	public void setSpawnEnabled(boolean enabled) {
		this.spawnEnabled = enabled;
		save();
	}

	public boolean isBackEnabled() {
		return backEnabled;
	}

	public void setBackEnabled(boolean enabled) {
		this.backEnabled = enabled;
		save();
	}

	public boolean isRtpEnabled() {
		return rtpEnabled;
	}

	public void setRtpEnabled(boolean enabled) {
		this.rtpEnabled = enabled;
		save();
	}

	/** Minimum distance in blocks between the player and the random teleport target. */
	public int getRtpRadius() {
		return rtpRadius;
	}

	public void setRtpRadius(int radius) {
		this.rtpRadius = Math.max(0, radius);
		save();
	}

	/** Delay between finding a spot and actually teleporting, in ticks. */
	public int getRtpCountdown() {
		return rtpCountdown;
	}

	public void setRtpCountdown(int ticks) {
		this.rtpCountdown = Math.max(0, ticks);
		save();
	}

	/** Per-player cooldown of {@code /rtp}, in ticks. */
	public int getRtpCooldown() {
		return rtpCooldown;
	}

	public void setRtpCooldown(int ticks) {
		this.rtpCooldown = Math.max(0, ticks);
		save();
	}

	public boolean isRtpAutoProtect() {
		return rtpAutoProtect;
	}

	public void setRtpAutoProtect(boolean value) {
		this.rtpAutoProtect = value;
		save();
	}

	/** How many simultaneous requests trigger the protection. */
	public int getRtpAutoProtectNum() {
		return rtpAutoProtectNum;
	}

	public void setRtpAutoProtectNum(int number) {
		this.rtpAutoProtectNum = Math.max(1, number);
		save();
	}

	public boolean isTpaEnabled() {
		return tpaEnabled;
	}

	public void setTpaEnabled(boolean enabled) {
		this.tpaEnabled = enabled;
		save();
	}

	/** Delay between accepting a request and the teleport, in ticks. */
	public int getTpaCountdown() {
		return tpaCountdown;
	}

	public void setTpaCountdown(int ticks) {
		this.tpaCountdown = Math.max(0, ticks);
		save();
	}

	/** Per-player cooldown of {@code /tpa} and {@code /tpahere}, in ticks. */
	public int getTpaCooldown() {
		return tpaCooldown;
	}

	public void setTpaCooldown(int ticks) {
		this.tpaCooldown = Math.max(0, ticks);
		save();
	}

	/** How long a teleport request waits for an answer, in ticks. */
	public int getTpaAutoDeny() {
		return tpaAutoDeny;
	}

	public void setTpaAutoDeny(int ticks) {
		this.tpaAutoDeny = Math.max(0, ticks);
		save();
	}

	public boolean isKickMeEnabled() {
		return kickMeEnabled;
	}

	public void setKickMeEnabled(boolean enabled) {
		this.kickMeEnabled = enabled;
		save();
	}

	public boolean isNicknameEnabled() {
		return nicknameEnabled;
	}

	public void setNicknameEnabled(boolean enabled) {
		this.nicknameEnabled = enabled;
		save();
	}

	public boolean isMuteEnabled() {
		return muteEnabled;
	}

	public void setMuteEnabled(boolean enabled) {
		this.muteEnabled = enabled;
		save();
	}

	/** Duration of a normal {@code /mute}, in ticks. */
	public int getMuteTimer() {
		return muteTimer;
	}

	public void setMuteTimer(int ticks) {
		this.muteTimer = Math.max(1, ticks);
		save();
	}

	public boolean isTrashcanEnabled() {
		return trashcanEnabled;
	}

	public void setTrashcanEnabled(boolean enabled) {
		this.trashcanEnabled = enabled;
		save();
	}

	public boolean isLoginEnabled() {
		return loginEnabled;
	}

	public void setLoginEnabled(boolean enabled) {
		this.loginEnabled = enabled;
		save();
	}

	/** {@code true} = simple mode (password only), {@code false} = normal mode. */
	public boolean isLoginSimpleMode() {
		return loginSimpleMode;
	}

	public void setLoginSimpleMode(boolean simple) {
		this.loginSimpleMode = simple;
		save();
	}

	/** Timeout for the first login (account registration), in ticks. */
	public int getLoginTimeFirst() {
		return loginTimeFirst;
	}

	public void setLoginTimeFirst(int ticks) {
		this.loginTimeFirst = Math.max(1, ticks);
		save();
	}

	/** Timeout for every login after the first one, in ticks. */
	public int getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(int ticks) {
		this.loginTime = Math.max(1, ticks);
		save();
	}

	public boolean isTreasuryEnabled() {
		return treasuryEnabled;
	}

	public void setTreasuryEnabled(boolean enabled) {
		this.treasuryEnabled = enabled;
		save();
	}

	/** {@code 0} means "no limit". */
	public long getTreasuryTransferLimit() {
		return treasuryTransferLimit;
	}

	public void setTreasuryTransferLimit(long limit) {
		this.treasuryTransferLimit = Math.max(0L, limit);
		save();
	}

	public boolean isAllowPlayerTransTreasury() {
		return allowPlayerTransTreasury;
	}

	public void setAllowPlayerTransTreasury(boolean value) {
		this.allowPlayerTransTreasury = value;
		save();
	}

	public boolean isAllowSpecialCurrency() {
		return allowSpecialCurrency;
	}

	public void setAllowSpecialCurrency(boolean value) {
		this.allowSpecialCurrency = value;
		save();
	}

	public boolean isAllowPlayerTranSPTreasury() {
		return allowPlayerTranSPTreasury;
	}

	public void setAllowPlayerTranSPTreasury(boolean value) {
		this.allowPlayerTranSPTreasury = value;
		save();
	}

	/** Display name of the base currency, used in every message that shows coins. */
	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String name) {
		this.currencyName = name == null || name.isBlank() ? "金币" : name;
		save();
	}

	public boolean isClaimEnabled() {
		return claimEnabled;
	}

	public void setClaimEnabled(boolean enabled) {
		this.claimEnabled = enabled;
		save();
	}

	/** {@code normal} = base currency, otherwise a special currency name. */
	public String getClaimCostCurrency() {
		return claimCostCurrency;
	}

	public void setClaimCostCurrency(String currency) {
		this.claimCostCurrency = currency == null || currency.isBlank() ? "normal" : currency;
		save();
	}

	/** {@code 0} means "free". */
	public long getClaimCostAmount() {
		return claimCostAmount;
	}

	public void setClaimCostAmount(long amount) {
		this.claimCostAmount = Math.max(0L, amount);
		save();
	}

	/** {@code 0} means "unlimited". */
	public int getClaimMaxNum() {
		return claimMaxNum;
	}

	public void setClaimMaxNum(int number) {
		this.claimMaxNum = Math.max(0, number);
		save();
	}

	/** {@code 0} forbids players from force-loading chunks at all. */
	public int getClaimForceloadMaxNum() {
		return claimForceloadMaxNum;
	}

	public void setClaimForceloadMaxNum(int number) {
		this.claimForceloadMaxNum = Math.max(0, number);
		save();
	}

	public boolean isOnlyOpPlayerClaim() {
		return onlyOpPlayerClaim;
	}

	public void setOnlyOpPlayerClaim(boolean value) {
		this.onlyOpPlayerClaim = value;
		save();
	}

	/** Shop id opened by a bare {@code /shop}; empty when none is configured. */
	public String getShopDefaultId() {
		return shopDefaultId;
	}

	public void setShopDefaultId(String id) {
		this.shopDefaultId = id == null ? "" : id.trim();
		save();
	}

	public boolean isShopEnabled() {
		return shopEnabled;
	}

	public void setShopEnabled(boolean enabled) {
		this.shopEnabled = enabled;
		save();
	}

	public boolean isMarketEnabled() {
		return marketEnabled;
	}

	public void setMarketEnabled(boolean enabled) {
		this.marketEnabled = enabled;
		save();
	}

	/** Default duration of a global market listing, in minutes. */
	public int getMarketDefaultMinutes() {
		return marketDefaultMinutes;
	}

	public void setMarketDefaultMinutes(int minutes) {
		this.marketDefaultMinutes = Math.max(1, minutes);
		save();
	}

	/** Master switch: while off, neither manual nor automatic sweeps run. */
	public boolean isSweepEnabled() {
		return sweepEnabled;
	}

	public void setSweepEnabled(boolean enabled) {
		this.sweepEnabled = enabled;
		save();
	}

	/** Automatic sweep interval in minutes; {@code 0} disables it. */
	public int getSweepTimeMinutes() {
		return sweepTimeMinutes;
	}

	public void setSweepTimeMinutes(int minutes) {
		this.sweepTimeMinutes = Math.max(0, minutes);
		save();
	}

	/** Comma separated countdown values in seconds, e.g. {@code 120,60,30}. */
	public String getSweepCountdownSeconds() {
		return sweepCountdownSeconds;
	}

	public void setSweepCountdownSeconds(String value) {
		this.sweepCountdownSeconds = value == null ? "" : value.trim();
		save();
	}

	public boolean isSilentSweep() {
		return silentSweep;
	}

	public void setSilentSweep(boolean value) {
		this.silentSweep = value;
		save();
	}

	/** {@code simple} = dropped items only, {@code entity} = monsters too. */
	public String getSweepMode() {
		return sweepMode;
	}

	public void setSweepMode(String mode) {
		this.sweepMode = "entity".equalsIgnoreCase(mode) ? "entity" : "simple";
		save();
	}

	public java.util.List<String> getSweepWhitelist() {
		if (sweepWhitelist == null) {
			sweepWhitelist = new java.util.ArrayList<>();
		}

		return sweepWhitelist;
	}

	/** @return {@code false} if the id is blank or already whitelisted */
	public boolean addSweepWhitelist(String id) {
		if (id == null || id.isBlank() || getSweepWhitelist().contains(id)) {
			return false;
		}

		sweepWhitelist.add(id);
		save();
		return true;
	}

	public boolean removeSweepWhitelist(String id) {
		boolean removed = getSweepWhitelist().remove(id);

		if (removed) {
			save();
		}

		return removed;
	}

	public boolean isSmartSweep() {
		return smartSweep;
	}

	public void setSmartSweep(boolean value) {
		this.smartSweep = value;
		save();
	}

	public double getSmartTpsThreshold() {
		return smartTpsThreshold;
	}

	public void setSmartTpsThreshold(double value) {
		this.smartTpsThreshold = Math.max(0.0, Math.min(20.0, value));
		save();
	}

	public int getSmartDropThreshold() {
		return smartDropThreshold;
	}

	public void setSmartDropThreshold(int value) {
		this.smartDropThreshold = Math.max(0, value);
		save();
	}

	public int getSmartScanCooldownSeconds() {
		return smartScanCooldownSeconds;
	}

	public void setSmartScanCooldownSeconds(int seconds) {
		this.smartScanCooldownSeconds = Math.max(1, seconds);
		save();
	}

	public int getSmartSweepCooldownSeconds() {
		return smartSweepCooldownSeconds;
	}

	public void setSmartSweepCooldownSeconds(int seconds) {
		this.smartSweepCooldownSeconds = Math.max(1, seconds);
		save();
	}
}
