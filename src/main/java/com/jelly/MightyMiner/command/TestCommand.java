package com.jelly.MightyMiner.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMiner.features.FeatureManager;
import com.jelly.MightyMiner.features.IFeature;
import com.jelly.MightyMiner.features.impl.commissionmacro.AutoCommissionClaim;
import com.jelly.MightyMiner.features.impl.general.AutoAotv;
import com.jelly.MightyMiner.features.impl.general.AutoInventory;
import com.jelly.MightyMiner.features.impl.general.AutoMithrilMiner;
import com.jelly.MightyMiner.features.impl.helper.RouteNode;
import com.jelly.MightyMiner.features.impl.helper.TransportMethod;
import com.jelly.MightyMiner.utils.BlockUtil;
import com.jelly.MightyMiner.utils.DrawUtils;
import com.jelly.MightyMiner.utils.RaytracingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(value = "set")
public class TestCommand {
    Minecraft mc = Minecraft.getMinecraft();
    List<Vec3> vecs = new ArrayList<>();
    List<Vec3> lines = new ArrayList<>();
    List<BlockPos> blocks = new ArrayList<>();
    BlockPos targetBlock = null;

    @Main
    public void main() {
//        AutoMithrilMiner.getInstance().enable(0, 0, false, true);
        List<RouteNode> LAVA_ETHERWARPLESS_1 = Arrays.asList(
            new RouteNode(new BlockPos(4, 160, -43), TransportMethod.FLY),
            new RouteNode(new BlockPos(9, 175, -12), TransportMethod.FLY),
            new RouteNode(new BlockPos(27, 206, -13), TransportMethod.FLY),
            new RouteNode(new BlockPos(54, 218, -12), TransportMethod.FLY),
            new RouteNode(new BlockPos(55, 226, -32), TransportMethod.FLY),
            new RouteNode(new BlockPos(56, 222, -30), TransportMethod.FLY)
        );
//        AutoAotv.getInstance().enable(LAVA_ETHERWARPLESS_1, false);
        AutoMithrilMiner.getInstance().enable(0, 0, false, false);
    }

    @SubCommand(aliases = {"stop", "sf"})
    public void stopfeatures() {
        FeatureManager.getInstance().loadFeatures().forEach(IFeature::disable);
    }

    @SubCommand
    public void clear() {
        vecs.clear();
        lines.clear();
        blocks.clear();
        targetBlock = null;
    }

    @SubCommand
    public void s(){
//        targetBlock = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
//        blocks.add(targetBlock);
        AutoCommissionClaim.getInstance().enable();
    }

    @SubCommand
    public void ab() {
        blocks.add(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        for (Vec3 vec : vecs) {
            DrawUtils.drawPoint(vec, Color.CYAN);
        }
        if (vecs.size() % 2 == 0) {
            for (int i = 0; i < vecs.size(); i += 2) {
                DrawUtils.drawLine(event, vecs.get(i), vecs.get(i + 1), 3f, Color.cyan);
            }
        }
        for (BlockPos blockPos : blocks) {
            DrawUtils.drawBlockBox(blockPos, new Color(255, 0, 0, 100));
        }
    }
}
