package com.jelly.MightyMiner.mixins.accessors;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftInvoker {
    @Invoker("clickMouse")
    void invokeLeftClickMouse();
    @Invoker("rightClickMouse")
    void invokeRightClickMouse();

    @Invoker("middleClickMouse")
    void invokeMiddleClickMouse();
}
