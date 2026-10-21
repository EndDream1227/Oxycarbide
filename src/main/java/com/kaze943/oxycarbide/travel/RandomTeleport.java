package com.kaze943.oxycarbide.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Finds random landing spots chunk by chunk.
 *
 * <p>Only one chunk is generated per call. The caller runs this once per server tick, so
 * the cost of terrain generation is spread over several ticks instead of stalling a
 * single tick (which is what made the old version freeze on far away chunks).</p>
 */
public final class RandomTeleport {
	/** How many random columns of a generated chunk are tested before giving up on it. */
	private static final int COLUMNS_PER_CHUNK = 8;

	private RandomTeleport() {
	}

	/**
	 * Picks a random chunk whose position is at least {@code minRadius} blocks away from
	 * the origin and still inside the world border.
	 *
	 * @return {@code null} if no valid chunk position was found
	 */
	public static ChunkPos pickChunk(ServerLevel level, double originX, double originZ,
			int minRadius, int maxRadius, RandomSource random) {
		WorldBorder border = level.getWorldBorder();

		for (int attempt = 0; attempt < 16; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double distance = minRadius + random.nextDouble() * (maxRadius - minRadius);
			int x = Mth.floor(originX + Math.cos(angle) * distance);
			int z = Mth.floor(originZ + Math.sin(angle) * distance);

			if (insideBorder(border, x, z)) {
				return new ChunkPos(x >> 4, z >> 4);
			}
		}

		return null;
	}

	/**
	 * Force-loads (and therefore generates) the given chunk, then looks for a safe
	 * surface column inside it.
	 *
	 * @return a safe position, or {@code null} if this chunk has none
	 */
	public static BlockPos findSafeSpot(ServerLevel level, ChunkPos chunk, RandomSource random) {
		// ChunkPos#x / #z are private in 26.3, so derive the chunk coordinates from the
		// minimum block position, which is always x << 4 / z << 4.
		int chunkX = chunk.getMinBlockX() >> 4;
		int chunkZ = chunk.getMinBlockZ() >> 4;
		level.getChunk(chunkX, chunkZ);

		for (int attempt = 0; attempt < COLUMNS_PER_CHUNK; attempt++) {
			int x = chunk.getMinBlockX() + random.nextInt(16);
			int z = chunk.getMinBlockZ() + random.nextInt(16);
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);

			if (isSafe(level, pos)) {
				return pos;
			}
		}

		return null;
	}

	/** Keeps a margin from the world border so the player cannot land outside of it. */
	private static boolean insideBorder(WorldBorder border, int x, int z) {
		double halfSize = border.getSize() / 2.0 - 32.0;
		return Math.abs(x + 0.5 - border.getCenterX()) <= halfSize
				&& Math.abs(z + 0.5 - border.getCenterZ()) <= halfSize;
	}

	private static boolean isSafe(ServerLevel level, BlockPos pos) {
		BlockPos ground = pos.below();
		BlockState below = level.getBlockState(ground);
		BlockState feet = level.getBlockState(pos);
		BlockState head = level.getBlockState(pos.above());

		// Two free blocks to stand in.
		if (!feet.isAir() || !head.isAir()) {
			return false;
		}

		// The block below must have a sturdy top face, so the player does not fall
		// through a slab-shaped or non-solid block.
		if (!below.isFaceSturdy(level, ground, Direction.UP) || !below.getFluidState().isEmpty()) {
			return false;
		}

		if (!feet.getFluidState().isEmpty() || !head.getFluidState().isEmpty()) {
			return false;
		}

		return !level.getBiome(pos).is(BiomeTags.IS_OCEAN);
	}
}
