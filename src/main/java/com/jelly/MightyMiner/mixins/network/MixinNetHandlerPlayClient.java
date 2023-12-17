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

    @Inject(method = "handleDisplayScoreboard", at = @At("RETURN"))
    public void handleUpdateScore(S3DPacketDisplayScoreboard packetIn, CallbackInfo ci){
        System.out.println();
        System.out.println("Position: " + packetIn.func_149371_c());
        System.out.println("Name: " + packetIn.func_149370_d());
        System.out.println();
        MinecraftForge.EVENT_BUS.post(new ScoreboardUpdateEvent());
    }
}
