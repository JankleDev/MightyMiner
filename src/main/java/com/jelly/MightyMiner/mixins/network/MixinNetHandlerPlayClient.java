package com.jelly.MightyMiner.mixins.network;

import com.jelly.MightyMiner.events.ScoreboardUpdateEvent;
import com.jelly.MightyMiner.handlers.MacroHandler;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {


    @Inject(method = "handlePlayerListHeaderFooter", at = @At("HEAD"))
    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn, CallbackInfo ci) {
        MacroHandler.gameState.header = packetIn.getHeader().getFormattedText().length() == 0 ? null : packetIn.getHeader();
        MacroHandler.gameState.footer = packetIn.getFooter().getFormattedText().length() == 0 ? null : packetIn.getFooter();
        MacroHandler.gameState.update();

    }

    // Turns out skyblock sucks a lot of balls
    // Like a lot
    // A lot lot
    // A massive amount
    // And it has to update its "Skyblock" text color which nobody gives a shit about
    // so this event is gonna fire way too often
    // but its better than checking every frame so yes
    // i still hat eskyblockforthis
    // credits to xai (xaine) and the other dude
    // i think both got banned
    @Inject(method = "handleScoreboardObjective", at = @At("RETURN"))
    public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn, CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new ScoreboardUpdateEvent());
    }
}
