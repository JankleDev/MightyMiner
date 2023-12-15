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

    // This is a chaotic function
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
        Entity maybeCeanna = mc.theWorld.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityOtherPlayerMP)
            .map(entity -> (EntityOtherPlayerMP) entity)
            .min(Comparator.comparingDouble(entity -> entity.getPositionVector().distanceTo(actualCeannaPos)))
            .orElse(null);
        if (maybeCeanna == null) return null;
        if (NpcUtil.isNpc(maybeCeanna)) return maybeCeanna;
        return null;
    }

    public static int getCompletedCommissionSlot() {
        for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
            if (!slot.getHasStack()) continue;
            if (InventoryUtils.getLoreFromSlotNumber(slot.slotNumber).contains("completed")) return slot.slotNumber;
        }
        return -1;
    }

    public static String getTransportItem() {
        if (MightyMiner.config.commTransportItem == 0) {
            return "Aspect of the End";
        } else {
            return "Aspect of the void";
        }
    }
}
