package com.jelly.MightyMiner.utils.helper;

import com.jelly.MightyMiner.features.impl.commissionmacro.AutoCommissionClaim;
import com.jelly.MightyMiner.features.impl.general.AutoMithrilMiner;
import com.jelly.MightyMiner.features.impl.general.AutoRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.world.World;

// ThankQ May2Bee :pepe_love_heart:
public interface Helper {
    Minecraft mc = Minecraft.getMinecraft();

    AutoCommissionClaim autoCommission = AutoCommissionClaim.getInstance();
    AutoRotation autoRotation = AutoRotation.getInstance();
    AutoMithrilMiner mithrilMiner = AutoMithrilMiner.getInstance();
}
