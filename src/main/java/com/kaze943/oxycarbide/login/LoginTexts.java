package com.kaze943.oxycarbide.login;

import com.kaze943.oxycarbide.text.JsonTexts;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Customisable login messages, loaded from
 * {@code config/oxycarbide/lang/zh/login_text.json} and
 * {@code config/oxycarbide/lang/en/login_text.json}.
 */
public final class LoginTexts {
	private static final Map<String, String> DEFAULT_ZH = JsonTexts.defaults(
			"welcome", "欢迎使用Oxycarbide，集服务器管理指令于一体",
			"prompt_register", "请使用/login <用户名> <密码>(或是单<密码>简单登录模式)设置你的登录信息，若未设置 %s 秒后将自动退出服务器！",
			"prompt_login", "请使用/login <用户名> <密码>(或是单<密码>简单登录模式)登录，%s 秒后将自动退出服务器",
			"success", "欢迎您！%s",
			"wrong", "用户名或密码错误",
			"username_taken", "该用户名已被占用",
			"mode_normal_hint", "当前为普通模式，请使用 /login <用户名> <密码>",
			"mode_simple_hint", "当前为简单模式，请使用 /login <密码>");

	private static final Map<String, String> DEFAULT_EN = JsonTexts.defaults(
			"welcome", "Welcome to Oxycarbide, your all-in-one server management mod.",
			"prompt_register", "Use /login <username> <password> (or <password> only in simple mode) to set your login, otherwise you will be kicked in %s second(s)!",
			"prompt_login", "Use /login <username> <password> (or <password> only in simple mode) to log in, otherwise you will be kicked in %s second(s).",
			"success", "Welcome, %s!",
			"wrong", "Wrong username or password.",
			"username_taken", "That username is already taken.",
			"mode_normal_hint", "This server uses normal mode, use /login <username> <password>.",
			"mode_simple_hint", "This server uses simple mode, use /login <password>.");

	private static final JsonTexts TEXTS = new JsonTexts("login_text.json", DEFAULT_ZH, DEFAULT_EN);

	private LoginTexts() {
	}

	public static void load() {
		TEXTS.load();
	}

	public static Component component(String key, Object... args) {
		return TEXTS.component(key, args);
	}
}
