package com.jelly.MightyMiner.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMiner.features.FeatureManager;
import com.jelly.MightyMiner.features.IFeature;
import com.jelly.MightyMiner.features.impl.commissionmacro.AutoCommissionClaim;
import com.jelly.MightyMiner.features.impl.general.AutoMithrilMiner;
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
import java.util.ArrayList;
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
//        AutoMithrilMiner.getInstance().enable(2753, 200, false, false);
//        BlockUtil.getMineableMithrilBlocks(false);
//        Vec3 v1 = new Vec3(-112.5, 168.01, -72.5);
//        Vec3 v2 = new Vec3(-110, 167.5, -75);
//        vecs.add(v1);
//        vecs.add(v2);
//        lines.add(v1);
//        lines.add(v2);
//        vecs.add(Minecraft.getMinecraft().thePlayer.getPositionVector());
//        MovingObjectPosition m = RaytracingUtils.raytrace(vecs.get(0), vecs.get(1));
//        if(m.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
//            blocks.add(m.getBlockPos());
//        }
//        BlockPos b = RaytracingUtils.getBlockLookingAt(5f);
//        this.targetBlock = b;
//        blocks.add(b);
//        List<EnumFacing> sides = RaytracingUtils.validSides(b);
//        System.out.println(sides);
//        for(EnumFacing side: sides){
//            float[] i = BlockUtil.BLOCK_SIDES.get(side);
//            vecs.add(new Vec3(b.getX()+i[0], b.getY()+i[1], b.getZ()+i[2]));
//        }
        AutoMithrilMiner.getInstance().enable(2753,200, false, true);
//        vecs.add(BlockUtil.bestPointOnBlock(this.targetBlock));
//        AutoCommissionClaim.getInstance().enable();
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
        targetBlock = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        blocks.add(targetBlock);
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
