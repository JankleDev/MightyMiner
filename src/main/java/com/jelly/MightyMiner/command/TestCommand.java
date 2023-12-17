package com.jelly.MightyMiner.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMiner.features.FeatureManager;
import com.jelly.MightyMiner.features.IFeature;
import com.jelly.MightyMiner.features.impl.commissionmacro.AutoCommissionClaim;
import com.jelly.MightyMiner.features.impl.general.*;
import com.jelly.MightyMiner.features.impl.helper.RouteNode;
import com.jelly.MightyMiner.features.impl.helper.TransportMethod;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.HypixelUtils.ScoreboardUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(value = "set")
public class TestCommand {
    Minecraft mc = Minecraft.getMinecraft();
    List<Vec3> vecs = new ArrayList<>();
    List<Vec3> lines = new ArrayList<>();
    List<BlockPos> blocks = new ArrayList<>();
    BlockPos targetBlock = null;

    @Main
    public void main() {
        AutoWarp.getInstance().enable(null, LocationTracker.SubLocation.THE_FORGE, false);
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
