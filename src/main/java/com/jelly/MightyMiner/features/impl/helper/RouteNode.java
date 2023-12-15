package com.jelly.MightyMiner.features.impl.helper;

import net.minecraft.util.BlockPos;

public class RouteNode {
    public final BlockPos blockPos;
    public final TransportMethod transportMethod;

    public RouteNode(BlockPos block, TransportMethod transportMethod) {
        this.blockPos = block;
        this.transportMethod = transportMethod;
    }
}
