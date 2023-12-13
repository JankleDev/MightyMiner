package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.utils.HypixelUtils.NpcUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.Slot;
import net.minecraft.util.Vec3;

import java.util.Comparator;

public class CommissionMacroUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static String getCommissionMacroTool() {
        switch (MightyMiner.config.commMiningTool) {
            case 0:
                return "Pickonimbus 2000";
            case 1:
                return "Gemstone Gauntlet";
            case 2:
                return "Titanium Drill DR-X";
            default:
                return "";
        }
    }

    // PE: I had a mental breakdown and had a schizo event when writing this function
    // Do not like when it uses hardcoded values
    // it DOES NOT Need these many checks but i heard hypixel added reach check which scares me
    // I overdid this i didnt add these many checks in meth (shameless plug) but something in me is gonna make
    // Me feel bad if people get banned for this
    public static Entity getCeanna() {
        final Vec3 actualCeannaPos = new Vec3(42.50, 134.50, 22.50);
        Entity ceannaNameStand = mc.theWorld.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityArmorStand)
            .filter(entity -> entity.getName() != null && entity.getName().contains("Ceanna"))
            .findFirst().orElse(null);
        if (ceannaNameStand != null) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!(entity instanceof EntityOtherPlayerMP)) continue;
                // It will probably never go worng the chances are too low... i cant even count that far help
                // hotsawp is balls bro
                if (VectorUtils.equals(ceannaNameStand.getPositionVector(), entity.getPositionVector()) && NpcUtil.isNpc(entity))
                    return entity;
            }
            return null;
        }
        // In case nametag didnt spawn/render (Does happen literally the first time i tried armorstand didnt spawn)

        Entity maybeCeanna = mc.theWorld.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityOtherPlayerMP)
            .map(entity -> (EntityOtherPlayerMP) entity)
            .min(Comparator.comparingDouble(entity -> entity.getPositionVector().distanceTo(actualCeannaPos)))
            .orElse(null);
        if (maybeCeanna == null) return null;
        // Did not use a distance check because hardcoded stuff is balls
        // Worse case -> Nametag Didnt Spawn/Some acoustic staff decided to change nametag
        //            -> or some weird thang like some weird mentally ill staff is satnding in the spot
        //            -> or some weird mentally ill star sentry was standing in the EXACT SPOT
        if (NpcUtil.isNpc(maybeCeanna)) return maybeCeanna;
        return null;
    }

    public static int getCompletedCommissionSlot(){
        for(Slot slot: mc.thePlayer.openContainer.inventorySlots){
            if(!slot.getHasStack()) continue;
            if(InventoryUtils.getLoreFromSlotNumber(slot.slotNumber).contains("completed")) return slot.slotNumber;
        }
        return -1;
    }
}
