package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.features.impl.commissionmacro.AutoCommissionClaim;
import com.jelly.MightyMiner.features.impl.general.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class FeatureManager {
    private static FeatureManager instance = null;

    public static FeatureManager getInstance() {
        if (instance == null) {
            instance = new FeatureManager();
        }
        return instance;
    }

    private List<IFeature> features;

    public List<IFeature> loadFeatures() {
        this.features = Arrays.asList(
            AutoRotation.getInstance(),
            AutoCommissionClaim.getInstance(),
            AutoMithrilMiner.getInstance(),
            AutoInventory.getInstance(),
            AutoAotv.getInstance(),
            AutoWarp.getInstance(),

            //Passive
            LocationTracker.getInstance(),
            InfoBarTracker.getInstance()
        );
        return this.features;
    }
}
