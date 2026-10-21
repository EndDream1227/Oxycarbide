package com.kaze943.oxycarbide.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

/**
 * One in-flight {@code /rtp}: either still searching for a chunk, or already waiting for
 * its countdown to run out.
 */
public final class RtpRequest {
	public enum State {
		SEARCHING,
		COUNTDOWN
	}

	private final UUID player;
	private final ServerLevel level;
	private final double originX;
	private final double originZ;
	private final int minRadius;
	private final int maxRadius;

	private int attemptsLeft;
	private State state = State.SEARCHING;
	private int ticksLeft;
	private BlockPos target;

	public RtpRequest(UUID player, ServerLevel level, double originX, double originZ,
			int minRadius, int maxRadius, int attempts) {
		this.player = player;
		this.level = level;
		this.originX = originX;
		this.originZ = originZ;
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;
		this.attemptsLeft = attempts;
	}

	public UUID player() {
		return player;
	}

	public ServerLevel level() {
		return level;
	}

	public double originX() {
		return originX;
	}

	public double originZ() {
		return originZ;
	}

	public int minRadius() {
		return minRadius;
	}

	public int maxRadius() {
		return maxRadius;
	}

	public int attemptsLeft() {
		return attemptsLeft;
	}

	public void consumeAttempt() {
		attemptsLeft--;
	}

	public State state() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public int ticksLeft() {
		return ticksLeft;
	}

	public void setTicksLeft(int ticksLeft) {
		this.ticksLeft = ticksLeft;
	}

	public void tickCountdown() {
		ticksLeft--;
	}

	public BlockPos target() {
		return target;
	}

	public void setTarget(BlockPos target) {
		this.target = target;
	}
}
