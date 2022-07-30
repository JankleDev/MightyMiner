package com.jelly.MightyMiner.baritone.baritones;

import com.jelly.MightyMiner.baritone.Baritone;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.structures.GridEnvironment;
import com.jelly.MightyMiner.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AutoMineBaritone extends Baritone{

    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    int maxX = 10000;
    int maxY = 256;
    int maxZ = 10000;

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();


    LinkedList<BlockPos> blocksToMine = new LinkedList<>();
    int step = 0;

    boolean inAction = false;
    Rotation rotation = new Rotation();

    int deltaJumpTick = 0;
    BlockPos lastMinedBlockPos;

    List<Block> forbiddenMiningBlocks;
    List<Block> allowedMiningBlocks;
    boolean shiftWhenMine;

    enum State{
        WALK,
        MINE,
        NONE
    }
    State currentState;

    public AutoMineBaritone(){}

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
    }

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
    }

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks, boolean shiftWhenMine){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
        this.shiftWhenMine = shiftWhenMine;
    }



    public void clearBlocksToWalk(){
        openNodes.clear();
        checkedNodes.clear();
        blocksToMine.clear();
        gridEnvironment.clear();
        BlockRenderer.renderMap.clear();
        lastMinedBlockPos = null;
        step = 0;
    }


    @Override
    protected void onEnable(BlockPos destinationBlock) throws Exception{
        mc.gameSettings.gammaSetting = 100;
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();
        mc.thePlayer.addChatMessage(new ChatComponentText("Starting automine"));


        BlockRenderer.renderMap.put(destinationBlock, Color.RED);
        blocksToMine = calculateBlocksToMine(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), destinationBlock);
        for(BlockPos blockPos : blocksToMine){
            BlockRenderer.renderMap.put(blockPos, Color.ORANGE);
        }
        BlockRenderer.renderMap.put(destinationBlock, Color.RED);
        inAction = true;
        currentState = State.NONE;
    }

    @Override
    public void onDisable() {
        inAction = false;
        KeybindHandler.resetKeybindState();
        clearBlocksToWalk();
    }


    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToMine != null){
                if(!blocksToMine.isEmpty()){
                    for(int i = 0; i < blocksToMine.size(); i++){
                        mc.fontRendererObj.drawString(blocksToMine.get(i).toString(), 5, 5 + 10 * i, -1);
                    }
                }
            }
        }
    }



    boolean walkFlag = false;
    @Override
    public void onTickEvent(TickEvent.Phase phase){

        if(phase == TickEvent.Phase.START) {

            if (inAction) {

                if(!blocksToMine.isEmpty()){
                    if ((!walkFlag && BlockUtils.isPassable(blocksToMine.getLast())) // if it is blocks to mine, check whether mined
                            || (walkFlag &&   // if it is blocks to walk, check -> possible to stand there || reached the target block
                            (!BlockUtils.fitsPlayer(blocksToMine.getLast()) || !BlockUtils.getPlayerLoc().equals(blocksToMine.getLast())))) {

                        lastMinedBlockPos = blocksToMine.getLast();
                        BlockRenderer.renderMap.remove(blocksToMine.getLast());
                        blocksToMine.removeLast();

                        walkFlag = !blocksToMine.isEmpty() && BlockUtils.isPassable(blocksToMine.getLast());
                    }
                }
                if (blocksToMine.isEmpty()) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("Finished baritone"));
                    inAction = false;
                    KeybindHandler.resetKeybindState();
                    disableBaritone();
                    return;
                }


                BlockPos targetMineBlock = blocksToMine.getLast();


                if (lastMinedBlockPos != null
                        && (lastMinedBlockPos.getY() == (int) mc.thePlayer.posY + 2 || lastMinedBlockPos.getY() == (int) mc.thePlayer.posY + 1)
                        && (lastMinedBlockPos.getX() != Math.floor(mc.thePlayer.posX) || lastMinedBlockPos.getZ() != Math.floor(mc.thePlayer.posZ))
                        && (BlockUtils.fitsPlayer(lastMinedBlockPos.down()) || BlockUtils.fitsPlayer(lastMinedBlockPos.down(2)))
                ) {
                    deltaJumpTick = 3;
                }


                KeybindHandler.updateKeys(
                        lastMinedBlockPos != null
                        && blocksToMine.size() > 1
                        && !targetMineBlock.equals(BlockUtils.getRelativeBlockPos(0, 2, 0))
                        && !targetMineBlock.equals(BlockUtils.getRelativeBlockPos(0, -1, 0)),
                        false,
                        false,
                        false,
                        mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null &&
                                mc.objectMouseOver.getBlockPos().equals(targetMineBlock) && !walkFlag,
                        false,
                        shiftWhenMine,
                        blocksToMine.size() > 1 && deltaJumpTick > 0);


                if (!BlockUtils.isPassable(targetMineBlock))
                    rotation.intLockAngle(AngleUtils.getRequiredYaw(targetMineBlock), AngleUtils.getRequiredPitch(targetMineBlock), 500);

                if (deltaJumpTick > 0)
                    deltaJumpTick--;
            }
        }


    }
    @Override
    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();

    }

    private LinkedList<BlockPos> calculateBlocksToMine(BlockPos startingPos, BlockPos endingBlock) throws Exception {

        if(startingPos.add(0, -1, 0).equals(endingBlock)){
            return new LinkedList<BlockPos>(){{add(startingPos.add(0, -1, 0));}};
        }
        if(BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock)){
            return new LinkedList<BlockPos>(){{add(endingBlock);}};
        }

        boolean completedPathfind = false;
        Node startNode;
        Node currentNode;
        Node goalNode = new Node(endingBlock);

        int currentGridX = maxX / 2;
        int currentGridY = (int) mc.thePlayer.posY;
        int currentGridZ = maxZ / 2;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        currentNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        startNode = currentNode;


        while (!completedPathfind) {
            step++;
            currentNode.checked = true;
            checkedNodes.add(currentNode);
            openNodes.remove(currentNode);

            if (currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX,  currentGridY, currentGridZ + 1), currentNode, endingBlock);
            }

            if (currentGridY > 0 && currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ + 1), currentNode, endingBlock);
            }

            if (currentGridY < maxY && currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ + 1), currentNode, endingBlock);
            }



            int bestIndex = 0;
            double minFcost = 9999;

            for (int i = 0; i < openNodes.size(); i++) {
                if(openNodes.get(i).hValue == 0){
                    bestIndex = i;
                    break;
                }
                if (openNodes.get(i).fValue < minFcost) {
                    bestIndex = i;
                    minFcost = openNodes.get(i).fValue;
                }
            }

            int tempX, tempY, tempZ;
            tempX = currentGridX;
            tempY = currentGridY;
            tempZ = currentGridZ;
            currentGridX += openNodes.get(bestIndex).blockPos.getX() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getX();
            currentGridY += openNodes.get(bestIndex).blockPos.getY() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getY();
            currentGridZ += openNodes.get(bestIndex).blockPos.getZ() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getZ();

            currentNode = openNodes.get(bestIndex);
            if (goalNode.blockPos.equals(currentNode.blockPos)) {
                completedPathfind = true;
            }
        }
        return trackBackPath(currentNode, startNode);



    }
    private void openNodeAndCalculateCost(Node searchNode, Node currentNode, BlockPos endingBlockPos){
        if ( (!searchNode.checked
                && !searchNode.opened
                && BlockUtils.canWalkOn(searchNode.blockPos.down()))){

            if(currentNode.lastNode != null){
                if(Math.abs(currentNode.lastNode.blockPos.getY() - searchNode.blockPos.getY()) > 1 &&
                        BlockUtils.onTheSameXZ(currentNode.lastNode.blockPos, searchNode.blockPos))
                    return;
            }
            if(!searchNode.blockPos.equals(endingBlockPos)) {
                if (forbiddenMiningBlocks != null && forbiddenMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos))  && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;

                if (allowedMiningBlocks != null && !allowedMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;
            }

            searchNode.opened = true;
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
            calculateCost(searchNode, endingBlockPos);
        }

    }

    private void instantiateNode(int gridX, int gridY, int gridZ, Node startNode){
        instantiateAnyNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX - maxX/2, gridY - startNode.blockPos.getY(), gridZ -  maxZ/2)));
    }
    private void instantiateAnyNode(int gridX, int gridY, int gridZ, Node node){
        if(gridEnvironment.get(gridX, gridY, gridZ) == null)
            gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    private void calculateCost(Node node, BlockPos endingBlock){
        node.hValue = MathUtils.getHeuristicCostBetweenTwoBlock(node.blockPos, endingBlock);

        if(node.lastNode != null) {
            if(node.lastNode.blockPos.getY() != node.blockPos.getY()) {
                node.gValue = node.lastNode.gValue + 2;
            } else {
                node.gValue = node.lastNode.gValue + 1;
            }
        }
        else
            node.gValue = 1f;
        node.fValue = node.gValue + node.hValue;
    }

    private LinkedList<BlockPos> trackBackPath(Node goalNode, Node startNode){
        LinkedList<BlockPos> blocksToMine = new LinkedList<>();

        Node formerNode = null;


        if(goalNode.blockPos != null && goalNode.lastNode != null && goalNode.lastNode.blockPos != null) {
            if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                blocksToMine.add(goalNode.blockPos);
                if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                    blocksToMine.add(goalNode.blockPos.up());
                }
            } else {
                blocksToMine.add(goalNode.blockPos);
                if(AngleUtils.shouldLookAtCenter(goalNode.blockPos) && !AngleUtils.shouldLookAtCenter(goalNode.blockPos.up())){
                    blocksToMine.add(goalNode.blockPos.up());
                }
            }
            formerNode = goalNode;
            goalNode = goalNode.lastNode;
        }


        if (goalNode.lastNode != null && formerNode != null) {

            do {
                if (formerNode.blockPos.getY() > goalNode.blockPos.getY()) {

                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2))) {
                        blocksToMine.add(goalNode.blockPos.up(2));
                    }
                    if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                        blocksToMine.add(goalNode.blockPos.up());
                    }

                    blocksToMine.add(goalNode.blockPos);
                } else if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                    blocksToMine.add(goalNode.blockPos);

                    if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                        blocksToMine.add(goalNode.blockPos.up());
                    }
                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2))) {
                        blocksToMine.add(goalNode.blockPos.up(2));
                    }

                } else {
                    blocksToMine.add(goalNode.blockPos);

                    if (!BlockUtils.isPassable(goalNode.blockPos))
                        blocksToMine.add(goalNode.blockPos.up());
                }
                formerNode = goalNode;
                goalNode = goalNode.lastNode;
            } while(!startNode.equals(goalNode) && goalNode.lastNode.blockPos != null);

           /* while (!startNode.equals(goalNode) && goalNode.lastNode.blockPos != null) {
                if (formerNode.blockPos.getY() > goalNode.blockPos.getY()) {

                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2))) {
                        blocksToMine.add(goalNode.blockPos.up(2));
                    }
                    if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                        blocksToMine.add(goalNode.blockPos.up());
                    }

                    blocksToMine.add(goalNode.blockPos);
                } else if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                    blocksToMine.add(goalNode.blockPos);

                    if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                        blocksToMine.add(goalNode.blockPos.up());
                    }
                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2))) {
                        blocksToMine.add(goalNode.blockPos.up(2));
                    }

                } else {
                    blocksToMine.add(goalNode.blockPos);

                    if (!BlockUtils.isPassable(goalNode.blockPos))
                        blocksToMine.add(goalNode.blockPos.up());
                }
                formerNode = goalNode;
                goalNode = goalNode.lastNode;
            }*/
        }

        mc.thePlayer.addChatMessage(new ChatComponentText(blocksToMine.getLast().toString()));
        //add back player extra block


        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToMine.size()));
        return blocksToMine;
    }

}
