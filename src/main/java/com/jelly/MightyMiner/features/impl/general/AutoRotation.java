package com.jelly.MightyMiner.features.impl.general;

// Old rot class isnt bad im having trouble using it
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.features.impl.helper.Angle;
import com.jelly.MightyMiner.features.impl.helper.Ease;
import com.jelly.MightyMiner.features.impl.helper.LockType;
import com.jelly.MightyMiner.features.impl.helper.Target;
import com.jelly.MightyMiner.utils.AngleUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Function;

public class AutoRotation extends AbstractFeature {
    private static AutoRotation instance = null;
    public static AutoRotation getInstance() {
        if (instance == null) {
            instance = new AutoRotation();
        }
        return instance;
    }
    private static final Minecraft mc = Minecraft.getMinecraft();

    private Function<Float, Float> easeFunction;

    private Target target;
    private Angle startAngle;

    private long endTime = 0L;
    private long startTime = 0L;

    private LockType lockType = LockType.NONE;
    private int smoothLockTime = 0;

    public void easeTo(Target target, int time) {
        note("Started easing");
        easeTo(target, time, LockType.NONE, 200, Ease.getRandomEaseFunction());
        note("Enable: " + this.canEnable());
    }

    public void easeTo(Target target, int time, LockType lockType, int smoothLockTime) {
        easeTo(target, time, lockType, smoothLockTime, Ease.getRandomEaseFunction());
    }

    public void easeTo(Target target, int time, LockType lockType, int smoothLockTime, Function<Float, Float> easeFunction) {
        this.enabled = true;
        this.forceEnable = true;
        this.lockType = lockType;
        this.smoothLockTime = smoothLockTime;

        this.easeFunction = easeFunction;
        this.startAngle = AngleUtils.getPlayerAngle();
        this.target = target;

        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + time;
    }

    private void changeAngle(float yawChange, float pitchChange) {
        float newYawChange = yawChange / 0.15f;
        float newPitchChange = pitchChange / 0.15f;
        mc.thePlayer.setAngles(newYawChange, newPitchChange);
    }

    private void interpolate(Angle startAngle, Angle endAngle) {
        float timeProgress = (float) (System.currentTimeMillis() - this.startTime) / (this.endTime - this.startTime);
        float totalNeededAngleProgress = this.easeFunction.apply(timeProgress);
        Angle totalChange = AngleUtils.calculateNeededAngleChange(this.startAngle, endAngle);

        float currentYawProgress = (mc.thePlayer.rotationYaw - startAngle.yaw) / totalChange.yaw;
        float currentPitchProgress = (mc.thePlayer.rotationPitch - startAngle.pitch) / totalChange.pitch;
        float yawProgressThisFrame = totalChange.yaw * (totalNeededAngleProgress - currentYawProgress);
        float pitchProgressThisFrame = totalChange.pitch * (totalNeededAngleProgress - currentPitchProgress);

        this.changeAngle(
            reduceTrailingPointsTo(yawProgressThisFrame, 2),
            -reduceTrailingPointsTo(pitchProgressThisFrame, 2)
        );
    }

    @Override
    public String getFeatureName() {
        return "AutoRotation";
    }

    @Override
    public boolean isPassiveFeature() {
        return false;
    }

    @Override
    public void disable() {
        enabled = false;
        forceEnable = false;
        easeFunction = null;

        startAngle = null;
        target = null;

        endTime = 0L;
        startTime = 0L;

        lockType = LockType.NONE;
        smoothLockTime = 0;
    }

    @Override
    public boolean canEnable() {
        return this.enabled;
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {

    }

    @Override
    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event) {
        if (!this.canEnable()) return;

        if (this.endTime >= System.currentTimeMillis()) {
            this.interpolate(this.startAngle, this.target.getAngle());
            return;
        }
        if (lockType == LockType.NONE) {
            this.disable();
            return;
        }

        if (lockType == LockType.INSTANT) {
            Angle angChange = AngleUtils.calculateNeededAngleChange(AngleUtils.getPlayerAngle(), this.target.getAngle());
            mc.thePlayer.rotationYaw += angChange.yaw;
            mc.thePlayer.rotationPitch += angChange.pitch;
        } else {
            this.easeTo(this.target, 200, LockType.SMOOTH, this.smoothLockTime, this.easeFunction);
        }
    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {
    }

    private float reduceTrailingPointsTo(float value, int number) {
        float multiplier = (float) Math.pow(10, number);
        return Math.round(value * multiplier) / multiplier;
    }
}
