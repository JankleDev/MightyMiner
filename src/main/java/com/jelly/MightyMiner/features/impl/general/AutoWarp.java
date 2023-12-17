package com.jelly.MightyMiner.features.impl.general;

import com.jelly.MightyMiner.features.AbstractFeature;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

import static com.jelly.MightyMiner.features.impl.general.LocationTracker.Location;
import static com.jelly.MightyMiner.features.impl.general.LocationTracker.SubLocation;

public class AutoWarp extends AbstractFeature {
    private static AutoWarp instance = null;

    public static AutoWarp getInstance() {
        if (instance == null) {
            instance = new AutoWarp();
        }
        return instance;
    }

    private int attempts = 0;
    private boolean notOnSkyBlock = false;
    private Location targetLocation = null;
    private SubLocation targetSubLocation = null;

    // Message
    private final String couldntWarp = "Couldn't warp you! Try again later.";
    private final String sendingCommandsTooFast = "You are sending commands too fast! Please slow down.";
    private final String playerNotOnSkyBlock = "Oops! You are not on SkyBlock so we couldn't warp you!";
    private final String noWarpScroll = "You haven't unlocked this fast travel destination!";
    private final String kickedWhileJoining = "You were kicked while joining that server!";
    private final String rejoinedTooFast = "You tried to rejoin too fast, please try again in a moment.";

    @Override
    public String getFeatureName() {
        return "AutoWarp";
    }

    @Override
    public boolean isPassiveFeature() {
        return false;
    }

    public void enable(Location targetLocation, SubLocation targetSubLocation, boolean forceEnable) {
        this.enabled = true;
        this.forceEnable = forceEnable;
        this.attempts = 0;
        this.targetLocation = targetLocation;
        this.targetSubLocation = targetSubLocation;
        this.notOnSkyBlock = false;

        log("Enabled");
    }

    @Override
    public void disable() {
        if (!this.enabled) return;
        this.enabled = false;
        this.forceEnable = false;
        this.attempts = 0;
        this.targetLocation = null;
        this.targetSubLocation = null;
        this.notOnSkyBlock = false;

        log("Disabled");
    }

    @Override
    public boolean canEnable() {
        return this.enabled || this.forceEnable;
    }

    @Override
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !this.canEnable()) return;

        if (this.attempts > 10) {
            this.setSuccessStatus(false);
            this.disable();

            log("Failed to Auto Warp.");
            return;
        }

        if (this.isDoneWarping()) {
            this.setSuccessStatus(true);
            this.disable();

            log("Done Warping");
            return;
        }

        if (!this.timer.passed()) return;

        this.attempts++;
        this.timer.schedule(5000); // 5s between each rewrap make customizable or change
        Location currentLocation = locationTracker.getLocation();
        SubLocation currentSubLocation = locationTracker.getSubLocation();

        if (this.notOnSkyBlock) {
            this.notOnSkyBlock = false;
            sendCommand(getWarpCommand(Location.LOBBY));

            log("Not On SkyBlock error");
            return;
        }

        if (!locationTracker.isInSkyblock()) {
            if (currentLocation == Location.LIMBO) {
                sendCommand(getWarpCommand(Location.LOBBY));
            } else {
                sendCommand("/play sb");
            }

            log("Player is not on skyblock.");
            return;
        }

        if (this.targetLocation != null && this.targetLocation != currentLocation) {
            String warpCommand = getWarpCommand(this.targetLocation);
            sendCommand(warpCommand);

            log("Player not at island. Sending Command: " + warpCommand);
            return;
        }

        if (this.targetSubLocation != null && this.targetSubLocation != currentSubLocation) {
            String warpCommand = getWarpCommand(this.targetSubLocation);
            sendCommand(warpCommand);

            log("Player not at SubLocation. Sending: " + warpCommand);
            return;
        }

        this.setSuccessStatus(false);
        this.disable();
        return;
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    @Override
    @SubscribeEvent
    public void onChatMessageReceive(ClientChatReceivedEvent event) {
        if (!this.canEnable()) return;
        if (event.type != 0) return;
        String message = event.message.getUnformattedText();


        String cannotJoinSB = "Cannot join SkyBlock for a moment!";
        if (message.contains(cannotJoinSB)
            || message.contains(couldntWarp)
            || message.contains(sendingCommandsTooFast)
            || message.contains(kickedWhileJoining)
            || message.contains(rejoinedTooFast)) {
            this.timer.schedule(10000); // Wait time
        }
        if (message.contains(playerNotOnSkyBlock)) {
            this.notOnSkyBlock = true;
        }
        if (message.contains(noWarpScroll)) {
            this.setSuccessStatus(false);
            this.disable();

            note("Please use the ${this.island!!.name} and ${this.subLocation!!.name} travel scrolls to unlock this destination.");
        }
    }

    public boolean isDoneWarping() {
        Location currentIsland = locationTracker.getLocation();
        SubLocation currentSubLocation = locationTracker.getSubLocation();

        return (this.targetLocation == null || currentIsland == this.targetLocation) &&
            (this.targetSubLocation == null || currentSubLocation == this.targetSubLocation);
    }

    private void sendCommand(String command) {
        mc.thePlayer.sendChatMessage(command);
    }

    private String getWarpCommand(Location location) {
        String command = LOCATION_WARP_COMMANDS.get(location);
        if (command.startsWith("/")) return command;
        return "/warp " + command;
    }

    private String getWarpCommand(SubLocation subLocation) {
        return "/warp " + SUBLOCATION_WARP_COMMANDS.get(subLocation);
    }

    private final HashMap<Location, String> LOCATION_WARP_COMMANDS = new HashMap<Location, String>() {{
        put(Location.PRIVATE_ISLAND, "island");
        put(Location.HUB, "hub");
        put(Location.THE_PARK, "park");
        put(Location.THE_FARMING_ISLANDS, "barn");
        put(Location.SPIDER_DEN, "spider");
        put(Location.THE_END, "end");
        put(Location.CRIMSON_ISLE, "isle");
        put(Location.GOLD_MINE, "gold");
        put(Location.DEEP_CAVERNS, "deep");
        put(Location.DWARVEN_MINES, "mines");
        put(Location.CRYSTAL_HOLLOWS, "ch");
        put(Location.JERRY_WORKSHOP, "/savethejerrys");
        put(Location.DUNGEON_HUB, "dhub");
        put(Location.LOBBY, "/l");
        put(Location.GARDEN, "garden");
    }};

    private final HashMap<SubLocation, String> SUBLOCATION_WARP_COMMANDS = new HashMap<SubLocation, String>() {{
        put(SubLocation.MUSEUM, "museum");
        put(SubLocation.SIRIUS_SHACK, "da");
        put(SubLocation.RUINS, "castle");
        put(SubLocation.MUSHROOM_DESERT, "desert");
        put(SubLocation.TRAPPERS_DEN, "trapper");
        put(SubLocation.HOWLING_CAVE, "howl");
        put(SubLocation.JUNGLE_ISLAND, "jungle");
        put(SubLocation.THE_FORGE, "forge");
        put(SubLocation.CRYSTAL_NUCLEUS, "nucleus");
        put(SubLocation.SPIDER_MOUND, "top");
        put(SubLocation.ARACHNES_SANCTUARY, "arachne");
        put(SubLocation.DRAGONS_NEST, "drag");
        put(SubLocation.VOID_SEPULTURE, "void");
        put(SubLocation.FORGOTTEN_SKULL, "skull");
        put(SubLocation.SMOLDERING_TOMB, "tomb");
        put(SubLocation.THE_WASTELAND, "wasteland");
        put(SubLocation.DRAGONTAIL, "dragontail");
        put(SubLocation.SCARLETON, "Scarleton");
    }};
}
