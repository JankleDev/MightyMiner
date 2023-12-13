package com.jelly.MightyMiner.features.impl.helper;

import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.Random;

public class Target {
    private Angle targetAngle;
    private Entity entity;
    private BlockPos block;
    private Vec3 vecPos;
    int change = 0;

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
            this.targetAngle = AngleUtils.getAngle(block);
        }
        if (entity != null) {
            this.targetAngle = AngleUtils.getAngle(entity, 1.5f);
        }
        if (vecPos != null) {
            this.targetAngle = AngleUtils.getAngle(vecPos);
        }
        return this.targetAngle;
    }

    @Override
    public String toString() {
        String type = (block != null) ? "Block" : (entity != null) ? "Entity" : (vecPos != null) ? "Vec3" : "Angle";
        return String.format("Target(type = %s, %s", type, this.targetAngle.toString());
    }
}