package com.jelly.MightyMiner.features.impl.commissionmacro;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.utils.Clock;
import com.jelly.MightyMiner.utils.CommissionMacroUtil;
import com.jelly.MightyMiner.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.swing.*;

public class AutoCommissionClaim extends AbstractFeature {
    private final Clock timer = new Clock(); // General Timer For Everything
    private final Minecraft mc = Minecraft.getMinecraft(); // Need to see if using one instance is better or not.
    private State currentState = State.STARTING;
    private final Vec3 ceannaPosition = new Vec3(42.50, 134.50, 22.50);

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
        note("Enabled");
    }

    @Override
    public void disable() {
        if (!this.enabled) return;

        this.enabled = false;
        this.timer.reset();

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

        switch (this.currentState){ // This is ugly kotlin "when" better

            case STARTING:
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
