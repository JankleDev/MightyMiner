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
    public static final int DYNAMIC_TIME = 0;
    private static final int DEFAULT_DYNAMIC_TIME = 500;
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

    private Angle lastAngle = null;

    private long endTime = 0L;
    private long startTime = 0L;

    private LockType lockType = LockType.NONE;
    private int smoothLockTime = 0;

    public void easeTo(Target target, int time) {
        easeTo(target, time, LockType.NONE, 200, Ease.getRandomEaseFunction());
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

        this.lastAngle = AngleUtils.getPlayerAngle();

        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + time;
        if (time == DYNAMIC_TIME) {
            this.endTime += calculateDynamicTimeFromAngleChange();
        }
    }

    private int calculateDynamicTimeFromAngleChange() {
        Angle angChange = AngleUtils.calculateNeededAngleChange(this.lastAngle, this.target.getAngle());
        int extraTime = getTime(Math.abs(angChange.yaw) + Math.abs(angChange.pitch));
        this.lastAngle = this.target.getAngle();
        return extraTime;
    }

    // Farmhelper Moment - Edit: Not really i ended up changing everything
    private int getTime(float change) {
        // Has to finish rotation under 3000ms - Change if you want
        // 150 because yes it looks ok - some sort of curve here would make it look better prob
        float progressLeftTime = Math.max(0, 1 - (int) (this.endTime - this.startTime) / (float) (6 * DEFAULT_DYNAMIC_TIME));
        return (int) (DEFAULT_DYNAMIC_TIME * (Math.min(1f, change / 150)) * progressLeftTime);
    }

    private void changeAngle(float yawChange, float pitchChange) {
        float newYawChange = clampDecimalsTo(yawChange / 0.15f, 2);
        float newPitchChange = clampDecimalsTo(pitchChange / 0.15f, 2);
        mc.thePlayer.setAngles(newYawChange, newPitchChange);
    }

    private void interpolate(Angle startAngle, Angle endAngle) {
        this.endTime += this.calculateDynamicTimeFromAngleChange();
        float timeProgress = (float) (System.currentTimeMillis() - this.startTime) / (this.endTime - this.startTime);
        float totalNeededAngleProgress = this.easeFunction.apply(timeProgress);
        Angle totalChange = AngleUtils.calculateNeededAngleChange(this.startAngle, endAngle);

        float currentYawProgress = (mc.thePlayer.rotationYaw - startAngle.yaw) / totalChange.yaw;
        float currentPitchProgress = (mc.thePlayer.rotationPitch - startAngle.pitch) / totalChange.pitch;
        float yawProgressThisFrame = totalChange.yaw * (totalNeededAngleProgress - currentYawProgress);
        float pitchProgressThisFrame = totalChange.pitch * (totalNeededAngleProgress - currentPitchProgress);

        this.changeAngle(yawProgressThisFrame, -pitchProgressThisFrame);
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
        this.enabled = false;
        this.forceEnable = false;
        this.easeFunction = null;

        // Crashed my game twice before figuring out i was setting it to null ffs
        Angle angleChange = AngleUtils.calculateNeededAngleChange(AngleUtils.getPlayerAngle(), this.target.getAngle());
        this.setSuccessStatus(Math.abs(angleChange.yaw) < .1 && Math.abs(angleChange.pitch) < .1);

        this.startAngle = null;
        this.target = null;

        this.lastAngle = null;

        this.endTime = 0L;
        this.startTime = 0L;

        this.lockType = LockType.NONE;
        this.smoothLockTime = 0;

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

    private float clampDecimalsTo(float value, int number) {
        float multiplier = (float) Math.pow(10, number);
        return Math.round(value * multiplier) / multiplier;
    }
}
