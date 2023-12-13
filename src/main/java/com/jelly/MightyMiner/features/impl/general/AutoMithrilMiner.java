package com.jelly.MightyMiner.features.impl.general;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.features.impl.helper.Target;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.List;

public class AutoMithrilMiner extends AbstractFeature {
    private static AutoMithrilMiner instance = null;

    public static AutoMithrilMiner getInstance() {
        if (instance == null) {
            instance = new AutoMithrilMiner();
        }
        return instance;
    }

    private boolean prioritizeTitanium = false;
    private int miningTicks = 0;
    private int miningSpeed = 0;
    private int miningSpeedBoost = 0;
    private boolean speedBoostActive = false;
    private boolean speedBoostAvailable = false;
    private BlockPos targetBlock = null;
    private Clock timer = new Clock();
    private State currentState = State.STARTING;

    enum State {
        STARTING, // ENGLISH
        GET_SPEED_AND_BOOST, // Gets Mining Speed and Speed Boost if None has been Provided
        SPEED_AND_BOOST_VERIFY, // Verifies that it successfully got speed and speed boost
        HANDLE_MSB, // Handles Mining Speed Boost When its Available (Right Clicks)
        CHECKING, // Checks for Blocks
        LOOKING, // Looks at Blocks
        LOOKING_VERIFY, // Verifies that it looked at the block
        BREAKING, // Breaks the block (With Tick Glide)
        BREAKING_VERIFY // Stops breaking when tick glide ends or however u say that
    }


    @Override
    public String getFeatureName() {
        return "MithrilMiner";
    }

    @Override
    public boolean isPassiveFeature() {
        return false;
    }

    public void enable(int miningSpeed, int miningSpeedBoost, boolean forceEnable, boolean prioritizeTitanium) {
        this.enabled = true;
        this.forceEnable = forceEnable;
        this.prioritizeTitanium = prioritizeTitanium;
        this.miningSpeed = miningSpeed;
        this.miningSpeedBoost = miningSpeedBoost;
        this.speedBoostAvailable = false;
        this.speedBoostActive = false;
        this.targetBlock = null;
        this.succeeded = false;
        this.failed = false;
        this.currentState = State.STARTING;

        note("Enabling");
    }

    @Override
    public void disable() {
        if (!this.canEnable()) return;

        KeybindHandler.holdLeftClick(false);

        this.enabled = true;
        this.forceEnable = false;
        this.prioritizeTitanium = false;
        this.miningSpeed = 0;
        this.miningSpeedBoost = 0;
        this.speedBoostAvailable = false;
        this.speedBoostActive = false;
        this.targetBlock = null;
        this.currentState = State.STARTING;

        note("Disabling");
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
                if (!InventoryUtils.setHotbarSlotForItem(CommissionMacroUtil.getCommissionMacroTool())) {
                    log("Cannot Hold Mining Tool");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                this.currentState = State.CHECKING;
                if (this.miningSpeed == 0 || this.miningSpeedBoost == 0) {
                    this.currentState = State.GET_SPEED_AND_BOOST;
                }
                if (this.speedBoostAvailable) {
                    this.currentState = State.HANDLE_MSB;
                    this.timer.schedule(500); // Delay before right clicking so that it doesnt get messed up
                    KeybindHandler.holdLeftClick(false);
                }

                log("Starting");
                break;
            case GET_SPEED_AND_BOOST:
                // TODO
                break;
            case SPEED_AND_BOOST_VERIFY:
                // TODO
                break;
            case HANDLE_MSB:
                if (!this.timer.passed()) return;

                KeybindHandler.rightClick();
                this.currentState = State.STARTING;
                this.speedBoostAvailable = false;
                break;
            case CHECKING:
                List<BlockPos> blocks = BlockUtil.getMineableMithrilBlocks(this.prioritizeTitanium);
                blocks.remove(this.targetBlock);

                if (blocks.isEmpty()) {
                    log("Cannot find any blocks to mine. Disabling");

                    this.setSuccessStatus(false);
                    this.disable();
                    return;
                }

                this.targetBlock = blocks.get(0);
                this.currentState = State.LOOKING;

                log("Finding");
                break;
            case LOOKING:
                Target rotationTarget = new Target(BlockUtil.bestPointOnBlock(this.targetBlock));
                autoRotation.easeTo(rotationTarget, MightyMiner.config.commRotationTime);
                this.timer.schedule(1000);
                this.currentState = State.LOOKING_VERIFY;

                log("Looking");
                break;
            case LOOKING_VERIFY:
                if (this.timer.passed() || autoRotation.hasFailed()) {
                    log("Could Not look at block. Disabling.");
                    this.setSuccessStatus(false);
                    this.disable();

                    return;
                }

                if (!BlockUtil.isBlockMithril(this.targetBlock)) {
                    log("Block changed to non mineable block (Probably Bedrock).");
                    this.currentState = State.STARTING;
                    return;
                }

                if (autoRotation.hasSucceeded() && this.targetBlock.equals(RaytracingUtils.getBlockLookingAt(5f))) {
                    this.currentState = State.BREAKING;
                    this.timer.reset();

                    log("Done Looking");
                }
                break;
            case BREAKING:
                int speed = (this.speedBoostActive) ? this.miningSpeed * (this.miningSpeedBoost / 100) : this.miningSpeed;
                this.miningTicks = BlockUtil.getBreakTicks(this.targetBlock, speed);
                KeybindHandler.holdLeftClick(true);
                this.currentState = State.BREAKING_VERIFY;

                log("Ticks: " + this.miningTicks);
                break;
            case BREAKING_VERIFY:
                this.miningTicks--;
                if (this.miningTicks < 0
                    || !BlockUtil.isBlockMithril(this.targetBlock)
                    || !this.targetBlock.equals(RaytracingUtils.getBlockLookingAt(5f))
                ) {
                    this.timer.reset();
                    this.currentState = State.STARTING;
                }

                log("Verifying Block Break");
                break;
        }
    }

    @Override
    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !this.canEnable() || this.targetBlock == null) return;
        DrawUtils.drawBlockBox(this.targetBlock, new Color(0, 255, 255, 150));
    }

    @Override
    @SubscribeEvent
    public void onChatMessageReceive(ClientChatReceivedEvent event) {
        if (event.type != 0) return;
    }
}
