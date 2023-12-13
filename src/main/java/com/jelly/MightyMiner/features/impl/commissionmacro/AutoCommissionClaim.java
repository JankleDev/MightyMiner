package com.jelly.MightyMiner.features.impl.commissionmacro;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.features.impl.general.AutoRotation;
import com.jelly.MightyMiner.features.impl.helper.Target;
import com.jelly.MightyMiner.utils.Clock;
import com.jelly.MightyMiner.utils.CommissionMacroUtil;
import com.jelly.MightyMiner.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoCommissionClaim extends AbstractFeature {
    private final Clock timer = new Clock(); // General Timer For Everything
    private final Minecraft mc = Minecraft.getMinecraft(); // Need to see if using one instance is better or not.
    private State currentState = State.STARTING;
    private Entity ceanna = null;
    private final Vec3 ceannaPosition = new Vec3(42.50, 134.50, 22.50);
    private int attempts = 0;

    enum State{
        STARTING,       // Self Explanatory
        LOOKING,        // Rotating at Ceanna
        LOOKING_VERIFY, // Verifying Successful Rotation
        OPENING_GUI,    // Opening Commission Claiming GUI Thing
        GUI_VERIFY,     // Verifying that player actually opened it
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

        this.enabled = false;
        this.timer.reset();
        this.ceanna = null;

        note("Disabled");
    }

    @Override
    public boolean canEnable() {
        // add failsafe check maybe
        return this.enabled || this.forceEnable;
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !this.canEnable()) return;

        if(this.attempts > 3){
            this.disable();
            this.setSuccessStatus(false);
            log("Cannot continue. Failed more than 3 times");
        }

        switch (this.currentState){ // This is ugly kotlin "when" better

            case STARTING:
                if(!this.timer.passed()) return;

                String itemToHold = CommissionMacroUtil.getCommissionMacroTool();
                this.currentState = State.LOOKING;

                if(MightyMiner.config.commUsePigeon){
                    itemToHold = "Royal Pigeon";
                    this.timer.schedule(300);
                    this.currentState = State.OPENING_GUI;
                }
                if(!InventoryUtils.setHotbarSlotForItem(itemToHold)){
                    this.setSuccessStatus(false);
                    this.disable();

                    this.error("Cannot Hold Item.");
                    return;
                }

                log("Starting");
                break;
            case LOOKING:
                this.ceanna = CommissionMacroUtil.getCeanna();
                this.currentState = State.LOOKING_VERIFY;

                if(this.ceanna == null){
                    this.attempts++;
                    this.currentState = State.STARTING;

                    log("Could not find Emissary Ceanna.");
                    return;
                }

                Target rotationTarget = new Target(this.ceanna, 1.5f);
                AutoRotation.getInstance().easeTo(rotationTarget, 500); // Should Make Configurable
                break;
            case LOOKING_VERIFY:

                break;
            case OPENING_GUI:
                break;
            case GUI_VERIFY:
                break;
            case CLAIMING_COMM:
                break;
            case FINISHING:
                break;
        }

    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {

    }

}
