package com.kaze943.oxycarbide.home;

/**
 * A single home of a player.
 *
 * <p>Fields are public and non-final on purpose: this class is serialized as-is by Gson
 * into the world's home file, and Gson works best with plain mutable fields and a
 * no-argument constructor.</p>
 */
public final class Home {
	public String name;
	public String dimension;
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;

	public Home() {
	}

	public Home(String name, String dimension, double x, double y, double z, float yaw, float pitch) {
		this.name = name;
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
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
}
