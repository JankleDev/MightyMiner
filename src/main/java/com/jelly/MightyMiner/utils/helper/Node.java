package com.jelly.MightyMiner.utils.helper;

import net.minecraft.util.BlockPos;

public class Node {
    BlockPos block;
    float cost;

    public Node(BlockPos block, float cost) {
        this.block = block;
        this.cost = cost;
    }
}
