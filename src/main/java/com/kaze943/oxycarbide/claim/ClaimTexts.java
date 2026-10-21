package com.kaze943.oxycarbide.claim;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable chunk-claim messages, loaded from
 * {@code config/oxycarbide/lang/zh/claim_text.json} and
 * {@code config/oxycarbide/lang/en/claim_text.json}.
 */
public final class ClaimTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"only_op", "只有管理员可以申领区块",
			"already_claimed", "%s 已经申领了该区块",
			"limit", "你最多只能申领 %s 个区块",
			"insufficient", "申领该区块需要 %s %s，你只有 %s",
			"claimed", "已申领区块（%s, %s）",
			"belong", "该区块属于 %s",
			"belong_settings", "禁足：%s，同队开放：%s，公开：%s，强制加载：%s，白名单人数：%s",
			"publicopen_on", "已将该区块开放给所有玩家",
			"publicopen_off", "已取消该区块的公开",
			"forceload_loaded", "已强制加载该区块",
			"forceload_already", "该区块已经在强制加载",
			"forceload_not_loaded", "该区块没有在强制加载",
			"forceload_cancelled", "已取消该区块的强制加载",
			"forceload_limit", "你最多只能强制加载 %s 个区块",
			"forceload_forbidden", "服务器禁止玩家强制加载区块",
			"no_claim", "脚下的区块尚未被申领",
			"not_owner", "该区块属于 %s，你没有权限",
			"transferred", "已将区块移交给 %s",
			"disclaimed", "已取消该区块的申领",
			"whitelist_added", "%s 已加入该区块白名单",
			"whitelist_removed", "%s 已从该区块白名单移除",
			"not_whitelisted", "%s 不在该区块白名单中",
			"grounded_on", "已开启该区块的禁足",
			"grounded_off", "已关闭该区块的禁足",
			"team_on", "已开启同队自动通行",
			"team_off", "已关闭同队自动通行",
			"protected", "该区块属于 %s，你无法在此交互",
			"grounded", "该区块属于 %s，禁止进入");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"only_op", "Only operators can claim chunks.",
			"already_claimed", "%s has already claimed this chunk.",
			"limit", "You can only claim %s chunk(s).",
			"insufficient", "Claiming this chunk costs %s %s, you only have %s.",
			"claimed", "Claimed chunk (%s, %s).",
			"belong", "This chunk belongs to %s",
			"belong_settings", "Grounded: %s, open to team: %s, public: %s, force-loaded: %s, whitelist size: %s",
			"publicopen_on", "This chunk is now open to everyone.",
			"publicopen_off", "This chunk is no longer public.",
			"forceload_loaded", "This chunk is now force-loaded.",
			"forceload_already", "This chunk is already force-loaded.",
			"forceload_not_loaded", "This chunk is not force-loaded.",
			"forceload_cancelled", "Force-loading cancelled for this chunk.",
			"forceload_limit", "You can only force-load %s chunk(s).",
			"forceload_forbidden", "Players are not allowed to force-load chunks on this server.",
			"no_claim", "The chunk you are standing in is not claimed.",
			"not_owner", "This chunk belongs to %s; you don't have permission.",
			"transferred", "Transferred the chunk to %s.",
			"disclaimed", "Removed the claim on this chunk.",
			"whitelist_added", "%s was added to the whitelist.",
			"whitelist_removed", "%s was removed from the whitelist.",
			"not_whitelisted", "%s is not on the whitelist.",
			"grounded_on", "Grounded mode enabled for this chunk.",
			"grounded_off", "Grounded mode disabled for this chunk.",
			"team_on", "Team access enabled for this chunk.",
			"team_off", "Team access disabled for this chunk.",
			"protected", "This chunk belongs to %s; you cannot interact here.",
			"grounded", "This chunk belongs to %s; entry is forbidden.");

	private static final JsonTexts TEXTS = new JsonTexts("claim_text.json", DEFAULT_ZH, DEFAULT_EN);

	private ClaimTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
