package com.jelly.MightyMiner.features.impl.helper;

import com.jelly.MightyMiner.utils.AngleUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class Target {
    private Angle targetAngle;
    private Entity entity;
    private BlockPos block;
    private Vec3 vecPos;

    public Target(Angle targetAngle) {
        this.targetAngle = targetAngle;
    }

    public Target(Entity entity, float height) {
        this.targetAngle = AngleUtils.getAngle(entity, height);
        this.entity = entity;
    }

    public Target(BlockPos block) {
        this.targetAngle = AngleUtils.getAngle(block);
        this.block = block;
    }

    public Target(Vec3 vecPos) {
        this.targetAngle = AngleUtils.getAngle(vecPos);
        this.vecPos = vecPos;
    }

    public Angle getAngle() {
        if (block != null) {
            return AngleUtils.getAngle(block);
        }
        if (entity != null) {
            return AngleUtils.getAngle(entity, 1.5f);
        }
        if (vecPos != null) {
            return AngleUtils.getAngle(vecPos);
        }
        return targetAngle;
    }
}