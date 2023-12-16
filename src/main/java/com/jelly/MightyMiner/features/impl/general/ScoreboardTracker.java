package com.jelly.MightyMiner.features.impl.general;

import com.jelly.MightyMiner.events.ScoreboardUpdateEvent;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ScoreboardTracker extends AbstractFeature {
    private static ScoreboardTracker instance = null;
    public static ScoreboardTracker getInstance(){
        if(instance == null){
            instance = new ScoreboardTracker();
        }
        return instance;
    }
    @Override
    public String getFeatureName() {
        return "ScoreboardTracker";
    }

    @Override
    public boolean isPassiveFeature() {
        return true;
    }

    @Override
    public void disable() {

    }

    @Override
    public boolean canEnable() {
        return true;
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {

    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {

    }

    @SubscribeEvent
    public void onScoreboardUpdate(ScoreboardUpdateEvent event){
        LogUtils.addNote("Updated");
    }
}
