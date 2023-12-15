package com.jelly.MightyMiner.features.impl.general;

import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.utils.InventoryUtils;
import javafx.util.Pair;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoInventory extends AbstractFeature {
    private static AutoInventory instance = null;

    public static AutoInventory getInstance() {
        if (instance == null) {
            instance = new AutoInventory();
        }
        return instance;
    }

    private TaskType mainTask = TaskType.NONE;

    enum TaskType {
        NONE, // Didnt need it but whatever
        MOVE_ITEMS_IN_INVENTORY,
        COLLECT_MINING_SPEED_AND_BOOST,
    }

    // Check Mining Speed And Boost
    private SpeedCheckState speedState = SpeedCheckState.OPEN_INVENTORY;
    private int miningSpeed = 0;
    private int miningSpeedBoost = 0;
    private int speedAttempts = 0;

    enum SpeedCheckState {
        OPEN_INVENTORY,
        GET_VALUES
    }

    @Override
    public String getFeatureName() {
        return "AutoInventory";
    }

    @Override
    public boolean isPassiveFeature() {
        return false;
    }

    @Override
    public void disable() {
        if (!this.enabled) return;

        this.enabled = false;
        this.forceEnable = false;
        this.timer.reset();

        // Reset Mining Speed and Boost Collector
        this.mainTask = TaskType.NONE;
        this.speedAttempts = 0;
        this.speedState = SpeedCheckState.OPEN_INVENTORY;

        log("Disabling.");
    }

    @Override
    public boolean canEnable() {
        return this.enabled || this.forceEnable;
    }

    @Override
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !this.canEnable()) return;
        switch (this.mainTask){
            case MOVE_ITEMS_IN_INVENTORY:
                break;
            case COLLECT_MINING_SPEED_AND_BOOST:
                this.handleSpeedAndBoostCheck();
                break;
            default:
                this.disable();
        }
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {

    }

    // <editor-fold desc="Get Mining Speed And Mining Speed Boost">

    public void collectSpeedAndBoost(){
        this.collectSpeedAndBoost(false);
    }

    public void collectSpeedAndBoost(boolean forceEnable){
        this.enabled = true;
        this.forceEnable = forceEnable;
        this.miningSpeed = 0;
        this.miningSpeedBoost = 0;
        this.speedAttempts = 0;
        this.failed = false;
        this.succeeded = false;
        this.mainTask = TaskType.COLLECT_MINING_SPEED_AND_BOOST;
        this.speedState = SpeedCheckState.OPEN_INVENTORY;

        log("Collecting Speed and Boost.");
    }

    private void handleSpeedAndBoostCheck() {
        if(this.speedAttempts > 5){
            log("Tried too many times but failed to get speed and boost. Disabling!");
            this.setSuccessStatus(false);
            this.disable();
            return;
        }
        switch (this.speedState) {
            case OPEN_INVENTORY:
                if (!this.timer.passed()) return;

                InventoryUtils.closeOpenGui();

                this.speedState = SpeedCheckState.GET_VALUES;
                this.timer.schedule(2000); // Should be configurable
                if (this.miningSpeed == 0) {
                    mc.thePlayer.sendChatMessage("/sbmenu");
                } else if (this.miningSpeedBoost == 0) {
                    mc.thePlayer.sendChatMessage("/hotm");
                } else {
                    log("Done getting Mining Speed and Mining Speed Boost.");
                    this.setSuccessStatus(true);
                    this.disable();
                }
                break;
            case GET_VALUES:
                if (!this.timer.passed()) return;

                this.speedAttempts++;
                this.speedState = SpeedCheckState.OPEN_INVENTORY;

                int profileSlot = InventoryUtils.getOpenContainerSlotNumber("SkyBlock Profile");
                int speedBoostSlot = InventoryUtils.getOpenContainerSlotNumber("Mining Speed Boost");

                boolean isValidContainer = mc.thePlayer.openContainer instanceof ContainerChest && mc.currentScreen instanceof GuiChest;
                boolean hasValidSlots = profileSlot != -1 || speedBoostSlot != -1;

                if (!isValidContainer || !hasValidSlots) {
                    log("Could not open menu. Retrying!");
                    return;
                }

                if (this.miningSpeed == 0) {
                    String lore = InventoryUtils.getLoreFromSlotNumber(profileSlot);
                    Matcher matcher = Pattern.compile("mining speed (\\d+,*\\d*)").matcher(lore);
                    if (!matcher.find()) {
                        log("Could not find Mining speed. Retrying");
                        return;
                    }
                    this.miningSpeed = Integer.parseInt(matcher.group(matcher.groupCount()).replace(",", ""));
                    log("Mining Speed: " + this.miningSpeed);
                    return;
                }

                if (this.miningSpeedBoost == 0) {
                    String lore = InventoryUtils.getLoreFromSlotNumber(speedBoostSlot);
                    Matcher matcher = Pattern.compile("\\+(\\d+)%").matcher(lore);
                    if (!matcher.find()) {
                        log("Could not find mining speed boost. Retrying!");
                        return;
                    }
                    this.miningSpeedBoost = Integer.parseInt(matcher.group(matcher.groupCount()).replace(",", ""));
                    log("Mining Speed Boost: " + this.miningSpeedBoost);
                }
                break;
        }
    }

    public Pair<Integer, Integer> getSpeedAndBoost(){
        return new Pair<>(this.miningSpeed, this.miningSpeedBoost);
    }

// </editor-fold>
}
