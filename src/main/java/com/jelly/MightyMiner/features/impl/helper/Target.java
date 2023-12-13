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
        change++;
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
//        int changem = change/10;
//        float rand = (float) ((new Random().nextDouble()*50) + 25) * changem;
//        Angle newAngle = new Angle(AngleUtils.getActualRotationYaw(this.targetAngle.yaw + rand), this.targetAngle.pitch);
//        if(changem >= 1){
//            change = 0;
//            LogUtils.debugLog("NewYaw: " + newAngle.yaw + " NewPitch: " + newAngle.pitch);
//        }
//        return newAngle;
    }
}