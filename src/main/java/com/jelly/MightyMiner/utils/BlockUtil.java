package com.jelly.MightyMiner.utils;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;

public class BlockUtil {
    // Rosegold is full of sexy
    // From GumTuneClient
    public static final Map<EnumFacing, float[]> BLOCK_SIDES = new HashMap<EnumFacing, float[]>() {{
        put(EnumFacing.DOWN, new float[]{0.5f, 0.01f, 0.5f});
        put(EnumFacing.UP, new float[]{0.5f, 0.99f, 0.5f});
        put(EnumFacing.WEST, new float[]{0.01f, 0.5f, 0.5f});
        put(EnumFacing.EAST, new float[]{0.99f, 0.5f, 0.5f});
        put(EnumFacing.NORTH, new float[]{0.5f, 0.5f, 0.01f});
        put(EnumFacing.SOUTH, new float[]{0.5f, 0.5f, 0.99f});
        put(null, new float[]{0.5f, 0.5f, 0.5f});
    }};

    private static final Map<Block, int[]> ALLOWED_MITHRIL = new HashMap<Block, int[]>() {{
        put(Blocks.prismarine, new int[]{0, 1, 2});
        put(Blocks.wool, new int[]{3, 7});
        put(Blocks.stained_hardened_clay, new int[]{9});
        put(Blocks.stone, new int[]{4});
    }};

    public static Vec3 getClosestSidePos(BlockPos block){
        return getSidePos(block, RaytracingUtils.getValidSide(block));
    }

    public static Vec3 getSidePos(BlockPos block, EnumFacing side) {
        float[] i = BLOCK_SIDES.get(side);
        return new Vec3(block.getX() + i[0], block.getY() + i[1], block.getZ() + i[2]);
    }
}
