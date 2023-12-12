package com.jelly.MightyMiner.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMiner.features.FeatureManager;
import com.jelly.MightyMiner.features.IFeature;
import com.jelly.MightyMiner.features.impl.general.AutoRotation;
import com.jelly.MightyMiner.features.impl.helper.LockType;
import com.jelly.MightyMiner.features.impl.helper.Target;
import net.minecraft.util.BlockPos;

@Command(value = "set")
public class TestCommand {
    @Main
    public void main() {
        BlockPos blockPos = new BlockPos(242, 73, 161);
        AutoRotation.getInstance().easeTo(new Target(blockPos), 500, LockType.SMOOTH, 200);
    }

    @SubCommand(aliases = {"stop", "sf"})
    public void stopfeatures(){
        FeatureManager.getInstance().loadFeatures().forEach(IFeature::disable);
    }
}
