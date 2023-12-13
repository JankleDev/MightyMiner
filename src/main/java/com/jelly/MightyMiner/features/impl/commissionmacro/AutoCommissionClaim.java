package com.jelly.MightyMiner.features.impl.commissionmacro;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.features.impl.general.AutoRotation;
import com.jelly.MightyMiner.features.impl.helper.Target;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.Clock;
import com.jelly.MightyMiner.utils.CommissionMacroUtil;
import com.jelly.MightyMiner.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoCommissionClaim extends AbstractFeature {
    public static AutoCommissionClaim instance = null;
    public static AutoCommissionClaim getInstance(){
        if(instance == null){
            instance = new AutoCommissionClaim();
        }
        return instance;
    }
    private State currentState = State.STARTING;
    private Entity ceanna = null;
    private final Vec3 ceannaPosition = new Vec3(42.50, 134.50, 22.50);
    private int attempts = 0;

    enum State {
        STARTING,       // Self Explanatory
        LOOKING,        // Rotating at Ceanna
        LOOKING_VERIFY, // Verifying Successful Rotation
        OPENING_GUI,    // Opening Commission Claiming GUI Thing
        GUI_VERIFY,     // Verifying that mc.thePlayer actually opened it
        CLAIMING_COMM,  // Claiming the Commission
        FINISHING       // I Dont know english
    }

    @Override
    public String getFeatureName() {
        return "AutoCommission";
    }

    @Override
    public boolean isPassiveFeature() {
        return false;
    }

    public void enable(){
        this.enable(false);
    }

    public void enable(boolean forceEnable) {
        this.enabled = true;
        this.forceEnable = forceEnable;
        this.failed = false;
        this.succeeded = false;
        this.timer.reset();
        this.ceanna = null;

        note("Enabled");
    }

    @Override
    public void disable() {
        if (!this.enabled) return;

        InventoryUtils.closeOpenGui();
        this.currentState = State.STARTING;
        this.enabled = false;
        this.timer.reset();
        this.ceanna = null;
        this.attempts = 0;

        note("Disabled");
    }

    @Override
    public boolean canEnable() {
        // add failsafe check maybe
        return this.enabled || this.forceEnable;
    }

    @Override
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !this.canEnable()) return;

        if (this.attempts > 3) {
            log("Cannot continue. Failed more than 3 times");

            this.disable();
            this.setSuccessStatus(false);
            return;
        }

        switch (this.currentState) { // This is ugly kotlin "when" better

            case STARTING:
                if (!this.timer.passed()) return;
                InventoryUtils.closeOpenGui(); // This is here so that when "attempt" kicks in it closes any gui and starts new

                String itemToHold = CommissionMacroUtil.getCommissionMacroTool();
                this.currentState = State.LOOKING;

                if (MightyMiner.config.commUsePigeon) {
                    itemToHold = "Royal Pigeon";
                    this.timer.schedule(300);
                    this.currentState = State.OPENING_GUI;
                }
                if (!InventoryUtils.setHotbarSlotForItem(itemToHold)) {
                    this.error("Cannot Hold Item.");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                log("Starting");
                break;
            case LOOKING:
                this.ceanna = CommissionMacroUtil.getCeanna();
                this.currentState = State.LOOKING_VERIFY;

                if (this.ceanna == null) {
                    this.increaseAttempt();

                    log("Error! isNull: " + (this.ceanna == null));
                    return;
                }

                Target rotationTarget = new Target(this.ceanna, 1.5f);
                AutoRotation.getInstance().easeTo(rotationTarget, 500); // Should Make Configurable
                break;
            case LOOKING_VERIFY:
                if (AutoRotation.getInstance().hasSucceeded()) {
                    this.currentState = State.OPENING_GUI;

                    log("Looking at Ceanna.");
                    return;
                }
                if (AutoRotation.getInstance().hasFailed()) {
                    this.increaseAttempt();

                    log("Rotation Failed. Restarting");
                    return;
                }
                break;
            case OPENING_GUI:
                this.currentState = State.GUI_VERIFY;
                this.timer.schedule(1000);

                if (MightyMiner.config.commUsePigeon) {
                    KeybindHandler.rightClick();
                } else if (mc.thePlayer.getDistanceToEntity(this.ceanna) < 4) {
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, this.ceanna);
                } else {
                    this.increaseAttempt();
                    log("Too far away from NPC.");
                    return;
                }
                break;
            case GUI_VERIFY:
                if (this.timer.passed()) {
                    this.increaseAttempt();

                    log("Could not open gui.");
                    return;
                }

                if ((mc.currentScreen instanceof GuiChest
                    || mc.thePlayer.openContainer instanceof ContainerChest)
                    && InventoryUtils.getInventoryName().contains("Commissions")) {
                    this.currentState = State.CLAIMING_COMM;
                    this.timer.schedule(500); // Click Delay
                }

                break;
            case CLAIMING_COMM:
                if (!this.timer.passed()) return;
                int commissionSlot = CommissionMacroUtil.getCompletedCommissionSlot();
                if (commissionSlot == -1) {
                    log("Cannot find Commission To Claim. Disabling with \"Failed\"!");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                InventoryUtils.clickOpenContainerSlot(commissionSlot);
                this.timer.schedule(400);
                this.currentState = State.FINISHING;
                break;
            case FINISHING:
                if (!this.timer.passed()) return;
                log("Finished");

                this.setSuccessStatus(true);
                this.disable();
                break;
        }

    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {

    }

    private void increaseAttempt() {
        this.currentState = State.STARTING;
        this.attempts++;
    }

}
