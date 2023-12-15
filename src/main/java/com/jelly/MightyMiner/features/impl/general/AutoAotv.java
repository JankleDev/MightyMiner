package com.jelly.MightyMiner.features.impl.general;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.features.impl.helper.*;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class AutoAotv extends AbstractFeature {
    private static AutoAotv instance = null;

    public static AutoAotv getInstance() {
        if (instance == null) {
            instance = new AutoAotv();
        }
        return instance;
    }

    private List<RouteNode> route = new ArrayList<>();
    private State currentState = State.STARTING;
    private int targetNodeIndex = 0;
    private RouteNode targetNode = null;

    enum State {
        STARTING, // So hard to understand
        FINDING_NEXT_BLOCK, // I need to be consistant with my naming
        LOOKING_AT_NEXT_BLOCK, // Yes
        LOOK_VERIFY, // send credit card to verify
        AOTV_OR_ETHERWARP, // mhm
        AOTV_OR_ETHERWARP_VERIFY, // skrt skrt
        END_VERIFY // s
    }

    private int getRotationTime() {
        // Should be customizable
        RouteNode prevNode = this.route.get(Math.max(0, this.targetNodeIndex - 1));
        if (prevNode.transportMethod == TransportMethod.ETHERWARP && this.targetNode.transportMethod == TransportMethod.ETHERWARP) {
            return 0; // Etherwarp look time
        }

        return 0; // Aotv look time
    }

    @Override
    public String getFeatureName() {
        return "AutoAotv";
    }

    @Override
    public boolean isPassiveFeature() {
        return false;
    }

    public void enable(List<RouteNode> route, boolean forceEnable) {
        if (route.isEmpty()) {
            log("Cannot enable because route is empty.");
            return;
        }

        this.enabled = true;
        this.forceEnable = forceEnable;
        this.targetNode = null;
        this.targetNodeIndex = 0;
        this.succeeded = false;
        this.failed = false;
        this.timer.reset();
        this.currentState = State.STARTING;
        this.route = route;

        log("Enabling.");
    }

    @Override
    public void disable() {
        if (!this.enabled) return;

        this.enabled = false;
        this.forceEnable = false;
        this.targetNode = null;
        this.targetNodeIndex = 0;
        this.timer.reset();
        this.currentState = State.STARTING;
        autoRotation.disable();

        log("Disabling");
    }

    @Override
    public boolean canEnable() {
        return this.enabled || this.forceEnable;
    }

    @Override
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null || !this.canEnable()) return;

        switch (this.currentState) {
            case STARTING:
                if (this.route.isEmpty()) {
                    log("Route is empty");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                this.currentState = State.FINDING_NEXT_BLOCK;

                InventoryUtils.setHotbarSlotForItem(CommissionMacroUtil.getTransportItem());
                break;
            case FINDING_NEXT_BLOCK:
                if (this.targetNodeIndex == this.route.size()) {
                    this.currentState = State.END_VERIFY;
                    this.timer.schedule(250);
                    return;
                }

                this.currentState = State.LOOKING_AT_NEXT_BLOCK;
                this.targetNode = this.route.get(this.targetNodeIndex);
                break;
            case LOOKING_AT_NEXT_BLOCK:
                this.currentState = State.LOOK_VERIFY;
                this.timer.schedule(1000);

                // Maybe lock here idk
                autoRotation.easeTo(new Target(this.targetNode.blockPos), this.getRotationTime(), LockType.INSTANT, 0); // Should Make Customizable
                break;
            case LOOK_VERIFY:
                if (this.timer.passed() || autoRotation.hasFailed()) {
                    log("Could not look at next block in time.");
                    this.setSuccessStatus(false);
                    this.disable();

                    return;
                }

                Angle angChange = AngleUtils.calculateNeededAngleChange(this.targetNode.blockPos);

                if (!autoRotation.hasSucceeded() && (Math.abs(angChange.yaw) > 1 && Math.abs(angChange.pitch) > 1)) return;

                this.timer.reset();
                this.currentState = State.AOTV_OR_ETHERWARP;

                if (this.targetNode.transportMethod == TransportMethod.ETHERWARP) {
                    KeybindHandler.holdShift(true);
                    this.timer.schedule(250); // Sneak time
                }
                break;
            case AOTV_OR_ETHERWARP:
                if (!this.timer.passed()) return;

                KeybindHandler.rightClick();
                autoRotation.disable();

                this.timer.schedule(1000); // Time to wait before stopping in case player didnt teleport
                this.currentState = State.AOTV_OR_ETHERWARP_VERIFY;
                break;
            case AOTV_OR_ETHERWARP_VERIFY:
                if (this.timer.passed()) {
                    log("Time passed did not teleport so im shutting off.");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                double distanceTraveledFromLastTick = Math.sqrt(mc.thePlayer.getDistanceSq(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ));
                double distanceToTargetNode = Math.sqrt(mc.thePlayer.getDistanceSqToCenter(this.targetNode.blockPos));
                boolean playerStandingOnTargetNode = this.targetNode.blockPos.equals(PlayerUtils.playerStandingPosition());
                boolean shouldCrashIntoNextBlock = !mc.theWorld.isAirBlock(this.targetNode.blockPos)
                    && this.targetNode.transportMethod == TransportMethod.FLY
                    && Math.sqrt(mc.thePlayer.getDistanceSqToCenter(this.targetNode.blockPos)) > 2;

                if (distanceTraveledFromLastTick < 4 && !playerStandingOnTargetNode && distanceToTargetNode > 3) return;

                this.timer.reset();
                KeybindHandler.holdShift(false);

                if(distanceToTargetNode > 7 || (shouldCrashIntoNextBlock && !mc.thePlayer.onGround)){
                    this.currentState = State.LOOKING_AT_NEXT_BLOCK;
                }
                else{
                    this.targetNodeIndex++;
                    this.currentState = State.FINDING_NEXT_BLOCK;
                }

                log("Verifying Next Block");
                break;
            case END_VERIFY:
                if(this.timer.passed()){
                    log("Could not verify reaching destination.");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                double playerDistanceToNextNode = Math.sqrt(mc.thePlayer.getDistanceSq(this.targetNode.blockPos));
                boolean playerStandingOnFinalNode = PlayerUtils.playerStandingPosition().equals(this.route.get(this.route.size()-1).blockPos);

                if(playerDistanceToNextNode > 1 && !playerStandingOnFinalNode) return;

                this.setSuccessStatus(true);
                this.disable();

                log("Reached end");

                break;
        }
    }

    @Override
    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event) {
        if(mc.thePlayer == null || mc.theWorld == null || !this.canEnable()) return;

        if(this.targetNode != null){
            DrawUtils.drawBlockBox(this.targetNode.blockPos, Color.CYAN);
        }
    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {

    }
}
