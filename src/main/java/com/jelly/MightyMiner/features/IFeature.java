package com.jelly.MightyMiner.features;

import net.minecraft.network.Packet;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public interface IFeature {
    String getFeatureName();
    boolean isPassiveFeature();
    void disable();
    boolean canEnable();
    void setSuccessStatus(boolean succeeded);
    boolean hasSucceeded();
    boolean hasFailed();

    public void onTick(TickEvent.ClientTickEvent event);
    public void onLastRender(RenderWorldLastEvent event);
    public void onChatMessageReceive(ClientChatReceivedEvent event);
//    public void onPacketReceived(Packet<?> packet);

    void log(String message);
    void note(String message);
    void error(String message);
}
