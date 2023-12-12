package com.jelly.MightyMiner.utils;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RaytracingUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static EnumFacing getValidSide(BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) return null;
        float dist = Float.MAX_VALUE;
        EnumFacing face = null;

        for (EnumFacing side : BlockUtil.BLOCK_SIDES.keySet()) {
            float distanceToSide = (float) playerHeadPos().distanceTo(BlockUtil.getSidePos(block, side));

            if (canSeeSide(block, side) && distanceToSide < dist) {
                if (side == null && face != null) continue;
                dist = distanceToSide;
                face = side;
            }
        }

        return face;
    }

    public static List<EnumFacing> validSides(BlockPos block) {
        List<EnumFacing> validSide = new ArrayList<>();
        for (EnumFacing face : BlockUtil.BLOCK_SIDES.keySet()) {
            if (canSeeSide(block, face)) {
                validSide.add(face);
            }
        }
        return validSide;
    }

    private static boolean canSeeSide(BlockPos block, EnumFacing side) {
        float[] i = BlockUtil.BLOCK_SIDES.get(side);
        Vec3 endVec = new Vec3(block.getX() + i[0], block.getY() + i[1], block.getZ() + i[2]);
        return canSeePoint(endVec);
    }

    public static boolean canSeePoint(Vec3 position) {
        Vec3 startVec = playerHeadPos();
        MovingObjectPosition result = raytrace(startVec, position);
        if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return false;
        }
        Vec3 r = result.hitVec;
        return Math.abs(r.xCoord - position.xCoord) < 0.1 && Math.abs(r.yCoord - position.yCoord) < 0.1 && Math.abs(r.zCoord - position.zCoord) < 0.1;
    }

    public static BlockPos getBlockLookingAt(float distance) {
        Vec3 endPos = playerHeadPos().add(VectorUtils.scaleVec(mc.thePlayer.getLookVec(), distance));
        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(playerHeadPos(), endPos);
        return (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) ? result.getBlockPos() : null;
    }

    // Another sexy rosegold
    // Credits GumTuneClient
    public static MovingObjectPosition raytrace(Vec3 v1, Vec3 v2) {
        Vec3 v3 = v2.subtract(v1);
        List<Entity> entities = mc.theWorld.getEntitiesInAABBexcluding(null,
            mc.thePlayer.getEntityBoundingBox().addCoord(v3.xCoord, v3.yCoord, v3.zCoord).expand(1.0, 1.0, 1.0),
            entity -> entity.isEntityAlive() && entity.canBeCollidedWith()
        );

        entities.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity));

        for (Entity entity : entities) {
            MovingObjectPosition intercept = entity.getEntityBoundingBox().expand(0.5, 0.5, 0.5).calculateIntercept(v1, v2);

            if (intercept != null) {
                return new MovingObjectPosition(entity, intercept.hitVec);
            }
        }

        return mc.theWorld.rayTraceBlocks(v1, v2, false, true, false);
    }

    private static Vec3 playerHeadPos() {
        return mc.thePlayer.getPositionEyes(1.0F);
    }
}
