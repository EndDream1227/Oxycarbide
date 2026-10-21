package com.kaze943.oxycarbide.login;

/**
 * One stored login account.
 *
 * <p>Passwords are never stored in plain text: {@link #hash} is the salted SHA-256 of the
 * password and {@link #salt} is the random per-account salt.</p>
 */
public final class Account {
	public String username = "";
	public String playerName = "";
	public String uuid = "";
	public String salt = "";
	public String hash = "";
}
