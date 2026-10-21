package com.kaze943.oxycarbide.warp;

import java.util.UUID;

/**
 * A server-wide warp point that every player can use.
 *
 * <p>Fields are public and non-final on purpose: this class is serialized as-is by Gson
 * into the world's warp file, and Gson works best with plain mutable fields and a
 * no-argument constructor.</p>
 */
public final class Warp {
	public String name;
	public String dimension;
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;
	/** UUID of the player that created the warp, as a string. */
	public String owner;
	/** Name of the creating player at the time the warp was created. */
	public String ownerName;

	public Warp() {
	}

	public Warp(String name, String dimension, double x, double y, double z, float yaw, float pitch,
			String owner, String ownerName) {
		this.name = name;
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.owner = owner;
		this.ownerName = ownerName;
	}

	public String name() {
		return name;
	}

	public String dimension() {
		return dimension;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double z() {
		return z;
	}

	public float yaw() {
		return yaw;
	}

	public float pitch() {
		return pitch;
	}

	public String ownerName() {
		return ownerName;
	}

	public boolean isOwnedBy(UUID player) {
		return owner != null && player != null && owner.equals(player.toString());
	}
}
