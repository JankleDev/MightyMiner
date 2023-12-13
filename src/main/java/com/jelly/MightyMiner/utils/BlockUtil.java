package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.features.impl.helper.Angle;
import com.jelly.MightyMiner.utils.helper.Helper;
import com.jelly.MightyMiner.utils.helper.MinHeap;
import com.jelly.MightyMiner.utils.helper.Node;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.*;

public class BlockUtil implements Helper {
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

    public static Vec3 getClosestSidePos(BlockPos block) {
        return getSidePos(block, RaytracingUtils.getValidSide(block));
    }

    public static Vec3 getSidePos(BlockPos block, EnumFacing side) {
        float[] i = BLOCK_SIDES.get(side);
        return new Vec3(block.getX() + i[0], block.getY() + i[1], block.getZ() + i[2]);
    }

    public static List<BlockPos> getMineableMithrilBlocks(boolean prioritizeTitanium) {
        MinHeap blocks = new MinHeap(1024);
        BlockPos playerFeet = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);

        int c = 0;
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 4; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos targetBlock = playerFeet.add(x, y, z);
                    Vec3 targetVec = new Vec3(targetBlock.getX() + .5, targetBlock.getY() + .5, targetBlock.getZ() + .5);
                    if (!isBlockMithril(targetBlock)) continue;
                    if (mc.thePlayer.getPositionEyes(1f).distanceTo(targetVec) > 4.5) continue;
                    if (RaytracingUtils.getValidSide(targetBlock) == null) continue;
                    c++;
                    if (prioritizeTitanium && mc.theWorld.getBlockState(targetBlock).getBlock() == Blocks.stone) {
                        blocks.add(new Node(targetBlock, 0));
                        continue;
                    }
                    blocks.add(new Node(targetBlock, blockCost(targetBlock)));
                }
            }
        }
        return blocks.getBlocks();
    }

    public static boolean isBlockMithril(BlockPos blockPos) {
        IBlockState state = mc.theWorld.getBlockState(blockPos);
        Block block = state.getBlock();
        int color = block.getMetaFromState(state);
        if (ALLOWED_MITHRIL.containsKey(block)) {
            for (int i : ALLOWED_MITHRIL.get(block)) {
                if (i == color) return true;
            }
        }
        return false;
    }

    private static float blockCost(BlockPos block) {
        float hardness = hardnessCost(block);
        Vec3 bestPoint = getClosestSidePos(block);

        Angle angChange = AngleUtils.calculateNeededAngleChange(bestPoint);

        float rot = Math.abs(angChange.yaw) + Math.abs(angChange.pitch);
        double distance = bestPoint.distanceTo(mc.thePlayer.getPositionEyes(1f));
        return (float) (rot * .15 + hardness * .5 + distance * .35);
    }

    public static int hardnessCost(BlockPos blockPos) {
        IBlockState state = mc.theWorld.getBlockState(blockPos);
        Block block = state.getBlock();
        int color = block.getMetaFromState(state);

        if (block.equals(Blocks.prismarine)) {
            return 3;
        } else if (block.equals(Blocks.stained_hardened_clay)) {
            return 1;
        } else if (block.equals(Blocks.wool)) {
            if (color == 7) return 5;
            else return 7;
        } else if (block.equals(Blocks.stone)) {
            return 7;
        }
        return 7;
    }

    public static int getBreakTicks(BlockPos block, int miningSpeed) {
//        int speed = (int) (Math.ceil((getMithrilStrength(block) * 30.0) / miningSpeed)
//            + (Math.ceil(ping.serverPing * 0.1 / 50.0) + config.mithrilMinerTickGlideOffset) * 2);

        int speed =
            (int) (Math.ceil((getMithrilStrength(block) * 30.0) / miningSpeed) + MightyMiner.config.fastMineOffset) * 2;
        return Math.max(speed, 4);
    }

    // Todo: Add More
    private static int getMithrilStrength(BlockPos blockPos) {
        IBlockState state = mc.theWorld.getBlockState(blockPos);
        Block block = state.getBlock();
        int color = block.getMetaFromState(state);

        if (block == Blocks.prismarine) {
            return 800;
        } else if (block == Blocks.stained_hardened_clay) {
            return 500;
        } else if (block == Blocks.wool) {
            return (color == 7) ? 500 : 1500;
        } else if (block == Blocks.stone && color == 4) {
            return 2000;
        } else {
            return 2000;
        }
    }

    public static Vec3 bestPointOnBlock(BlockPos block) {
        Vec3 point = pointsOnBlockVisible(block).stream()
            .filter(RaytracingUtils::canSeePoint)
            .min((vec1, vec2) -> {
                Angle angleChange1 = AngleUtils.calculateNeededAngleChange(vec1);
                Angle angleChange2 = AngleUtils.calculateNeededAngleChange(vec2);

                float sum1 = Math.abs(angleChange1.yaw) + Math.abs(angleChange1.pitch);
                float sum2 = Math.abs(angleChange2.yaw) + Math.abs(angleChange2.pitch);
                return Float.compare(sum1, sum2);
            }).orElse(null);
        if (point == null) {
            return new Vec3(block).addVector(0.5, 0.5, 0.5);
        }
        return point;
    }

    public static List<Vec3> pointsOnBlockVisible(BlockPos block) {
        List<Vec3> points = new ArrayList<>();
        RaytracingUtils.validSides(block).forEach(face ->
            points.addAll(pointsOnBlockSide(block, face))
        );
        return points;
    }

    // Yet another rosegold sexy
    private static float randomVal() {
        return (float) (3 + new Random().nextInt(7)) / 10f;
    }

    private static List<Vec3> pointsOnBlockSide(BlockPos block, EnumFacing side) {
        List<Vec3> points = new ArrayList<>();
        float[] it = BLOCK_SIDES.get(side);

        if (side != null) {
            for (int i = 0; i < 20; i++) {
                float x = it[0];
                float y = it[1];
                float z = it[2];
                if (x == .5f) x = randomVal();
                if (y == .5f) y = randomVal();
                if (z == .5f) z = randomVal();
                Vec3 point = new Vec3(block).addVector(x, y, z);
                if (!points.contains(point)) points.add(point);
            }
        } else {
            for (float[] bside : BLOCK_SIDES.values()) {
                for (int i = 0; i < 20; i++) {
                    float x = bside[0];
                    float y = bside[1];
                    float z = bside[2];

                    if (x == .5f) x = randomVal();
                    if (y == .5f) y = randomVal();
                    if (z == .5f) z = randomVal();

                    Vec3 point = new Vec3(block).addVector(x, y, z);
                    if (!points.contains(point)) points.add(point);
                }
            }
        }
        return points;
    }
}
