package com.jelly.MightyMiner.features.impl.general;

import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.utils.HypixelUtils.ScoreboardUtils;
import com.jelly.MightyMiner.utils.TablistUtils;
import lombok.Getter;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationTracker extends AbstractFeature {
    private static LocationTracker instance = null;

    public static LocationTracker getInstance() {
        if (instance == null) {
            instance = new LocationTracker();
        }
        return instance;
    }

    private final Pattern areaPattern = Pattern.compile("Area:\\s(.+)");
    @Getter
    private Location location = Location.NOWHERE;
    @Getter
    private SubLocation subLocation = SubLocation.NOWHERE;
    private boolean worldChanging = false;

    @Override
    public String getFeatureName() {
        return "LocationTracker";
    }

    @Override
    public boolean isPassiveFeature() {
        return true;
    }

    @Override
    public void disable() {
    }

    @Override
    public boolean canEnable() {
        return true; // Should be MacroHandler.enabled
    }

    public boolean isInSkyblock(){
        return this.location != Location.LIMBO && this.location != Location.LOBBY;
    }

    public enum Location {
        PRIVATE_ISLAND("Private Island"),
        HUB("Hub"),
        THE_PARK("The Park"),
        THE_FARMING_ISLANDS("The Farming Islands"),
        SPIDER_DEN("Spider's Den"),
        THE_END("The End"),
        CRIMSON_ISLE("Crimson Isle"),
        GOLD_MINE("Gold Mine"),
        DEEP_CAVERNS("Deep Caverns"),
        DWARVEN_MINES("Dwarven Mines"),
        CRYSTAL_HOLLOWS("Crystal Hollows"),
        JERRY_WORKSHOP("Jerry's Workshop"),
        DUNGEON_HUB("Dungeon Hub"),
        LIMBO("UNKNOWN"),
        LOBBY("PROTOTYPE"),
        GARDEN("Garden"),
        DUNGEON("Dungeon"),
        NOWHERE("Nowhere");

        public final String name;

        Location(String name) {
            this.name = name;
        }
    }

    // I know this is useless bite me
    public enum SubLocation {
        NOWHERE("Nowhere"),

        // <editor-fold desc="The Hub">
        ARCHERY_RANGE("Archery Range"),
        AUCTION_HOUSE("Auction House"),
        BANK("Bank"),
        BAZAAR_ALLEY("Bazaar Alley"),
        BLACKSMITHS_HOUSE("Blacksmith's House"),
        BUILDERS_HOUSE("Builder's House"),
        CANVAS_ROOM("Canvas Room"),
        COAL_MINE("Coal Mine"),
        COLOSSEUM_ARENA("Colosseum Arena"),
        COLOSSEUM("Colosseum"),
        COMMUNITY_CENTER("Community Center"),
        ELECTION_ROOM("Election Room"),
        FARM("Farm"),
        FARMHOUSE("Farmhouse"),
        FASHION_SHOP("Fashion Shop"),
        FISHERMANS_HUT("Fisherman's Hut"),
        FLOWER_HOUSE("Flower House"),
        FOREST("Forest"),
        GRAVEYARD("Graveyard"),
        HEXATORUM("Hexatorum"),
        LIBRARY("Library"),
        MOUNTAIN("Mountain"),
        MUSEUM("Museum"),
        REGALIA_ROOM("Regalia Room"),
        RUINS("Ruins"),
        SHENS_AUCTION("Shen's Auction"),
        TAVERN("Tavern"),
        THAUMATURGIST("Thaumaturgist"),
        UNINCORPORATED("Unincorporated"),
        VILLAGE("Village"),
        WILDERNESS("Wilderness"),
        WIZARD_TOWER("Wizard Tower"),

        // These locations dont exist
        SIRIUS_SHACK("Sirius Shack"),
        CRYPTS("Crypts"),
        // </editor-fold>

        // <editor-fold desc="The Park">
        BIRCH_PARK("Birch Park"),
        DARK_THICKET("Dark Thicket"),
        HOWLING_CAVE("Howling Cave"),
        JUNGLE_ISLAND("Jungle Island"),
        LONELY_ISLAND("Lonely Island"),
        MELODYS_PLATEAU("Melody's Plateau"),
        SAVANNA_WOODLAND("Savanna Woodland"),
        SPRUCE_WOODS("Spruce Woods"),
        VIKING_LONGHOUSE("Viking Longhouse"),
        // </editor-fold>

        // <editor-fold desc="The Farming Islands">
        DESERT_SETTLEMENT("Desert Settlement"),
        GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave"),
        JAKES_HOUSE("Jake's House"),
        MUSHROOM_DESERT("Mushroom Desert"),
        MUSHROOM_GORGE("Mushroom Gorge"),
        OASIS("Oasis"),
        OVERGROWN_MUSHROOM_CAVE("Overgrown Mushroom Cave"),
        SHEPHERDS_KEEP("Shepherd's Keep"),
        TRAPPERS_DEN("Trapper's Den"),
        TREASURE_HUNTER_CAMP("Treasure Hunter Camp"),
        // </editor-fold>

        // <editor-fold desc="Deep Caverns">
        DIAMOND_RESERVE("Diamond Reserve"),
        GUNPOWDER_MINES("Gunpowder Mines"),
        LAPIS_QUARRY("Lapis Quarry"),
        OBSIDIAN_SANCTUARY("Obsidian Sanctuary"),
        PIGMENS_DEN("Pigmen's Den"),
        SLIMEHILL("Slimehill"),
        // </editor-fold>

        // <editor-fold desc="Dwarven Mines">
        ARISTOCRAT_PASSAGE("Aristocrat Passage"),
        BARRACKS_OF_HEROES("Barracks of Heroes"),
        C_AND_C_MINECARTS_CO("C&C Minecarts Co."),
        CLIFFSIDE_VEINS("Cliffside Veins"),
        DIVANS_GATEWAY("Divan's Gateway"),
        DWARVEN_MINES("Dwarven Mines"),
        DWARVEN_TAVERN("Dwarven Tavern"),
        DWARVEN_VILLAGE("Dwarven Village"),
        FAR_RESERVE("Far Reserve"),
        FORGE_BASIN("Forge Basin"),
        GATES_TO_THE_MINES("Gates to the Mines"),
        GOBLIN_BURROWS("Goblin Burrows"),
        GRAND_LIBRARY("Grand Library"),
        GREAT_ICE_WALL("Great Ice Wall"),
        HANGING_COURT("Hanging Court"),
        LAVA_SPRINGS("Lava Springs"),
        MINERS_GUILD("Miner's Guild"),
        PALACE_BRIDGE("Palace Bridge"),
        RAMPARTS_QUARRY("Rampart's Quarry"),
        ROYAL_MINES("Royal Mines"),
        ROYAL_PALACE("Royal Palace"),
        ROYAL_QUARTERS("Royal Quarters"),
        THE_FORGE("The Forge"),
        THE_LIFT("The Lift"),
        THE_MIST("The Mist"),
        UPPER_MINES("Upper Mines"),
        // </editor-fold>

        // <editor-fold desc="Crystal Hollows">
        CRYSTAL_NUCLEUS("Crystal Nucleus"), // IDK if this exists or not
        CRYSTAL_HOLLOWS("Crystal Hollows"),
        CRYSTAL_NUCLEUS_DRAGONS_LAIR("Crystal Nucleus Dragon's Lair"),
        FAIRY_GROTTO("Fairy Grotto"),
        GOBLIN_HOLDOUUT("Goblin Holdout"),
        GOBLIN_QUEENS_DEN("Goblin Queen's Den"),
        JUNGLE_TEMPLE("Jungle Temple"),
        JUNGLE("Jungle"),
        KHAZAD_DUM("Khazad-dûm"),
        LOST_PRECURSOR_CITY("Lost Precursor City"),
        MAGMA_FIELDS("Magma Fields"),
        MINES_OF_DIVAN("Mines of Divan"),
        MITHRIL_DEPOSITS("Mithril Deposits"),
        PRECURSOR_REMNANTS("Precursor Remnants"),
        // </editor-fold>

        // <editor-fold desc="Spider's den">
        ARACHNES_BURROW("Arachne's Burrow"),
        ARACHNES_SANCTUARY("Arachne's Sanctuary"),
        ARCHAEOLOGISTS_CAMP("Archaeologist's Camp"),
        GRANDMAS_HOUSE("Grandma's House"),
        GRAVEL_MINES("Gravel Mines"),
        SPIDER_MOUND("Spider Mound"),
        // </editor-fold>

        // <editor-fold desc="The end">
        DRAGONS_NEST("Dragon's Nest"),
        THE_END("The End"),
        VOID_SEPULTURE("Void Sepulture"),
        VOID_SLATE("Void Slate"),
        ZEALOT_BRUISER_HIDEOUT("Zealot Bruiser Hideout"),
        // </editor-fold>

        // <editor-fold desc="Crimson Isle">
        AURAS_LAB("Aura's Lab"),
        BARBARIAN_OUTPOST("Barbarian Outpost"),
        BELLY_OF_THE_BEAST("Belly of the Beast"),
        BLAZING_VOLCANO("Blazing Volcano"),
        BURNING_DESERT("Burning Desert"),
        CATHEDRAL("Cathedral"),
        CHIEFS_HUT("Chief's Hut"),
        //        COMMUNITY_CENTER("Community Center"), Duplicate
        COURTYARD("Courtyard"),
        CRIMSON_FIELDS("Crimson Fields"),
        DOJO("Dojo"),
        DRAGONTAIL_AUCTION_HOUSE("Dragontail Auction House"),
        DRAGONTAIL_BANK("Dragontail Bank"),
        DRAGONTAIL_BAZAAR("Dragontail Bazaar"),
        DRAGONTAIL_BLACKSMITH("Dragontail Blacksmith"),
        DRAGONTAIL_MINION_SHOP("Dragontail Minion Shop"),
        DRAGONTAIL_TOWNSQUARE("Dragontail Townsquare"),
        DRAGONTAIL("Dragontail"),
        FORGOTTEN_SKULL("Forgotten Skull"),
        IGRUPANS_CHICKEN_COOP("Igrupan's Chicken Coop"),
        IGRUPANS_HOUSE("Igrupan's House"),
        MAGE_COUNCIL("Mage Council"),
        MAGE_OUTPOST("Mage Outpost"),
        MAGMA_CHAMBER("Magma Chamber"),
        MATRIARCHS_LAIR("Matriarch's Lair"),
        MYSTIC_MARSH("Mystic Marsh"),
        ODGERS_HUT("Odger's Hut"),
        PLHLEGBLAST_POOL("Plhlegblast Pool"),
        RUINS_OF_ASHFANG("Ruins of Ashfang"),
        SCARLETON_AUCTION_HOUSE("Scarleton Auction House"),
        SCARLETON_BANK("Scarleton Bank"),
        SCARLETON_BAZAAR("Scarleton Bazaar"),
        SCARLETON_BLACKSMITH("Scarleton Blacksmith"),
        SCARLETON_MINION_SHOP("Scarleton Minion Shop"),
        SCARLETON_PLAZA("Scarleton Plaza"),
        SCARLETON("Scarleton"),
        SMOLDERING_TOMB("Smoldering Tomb"),
        STRONGHOLD("Stronghold"),
        THE_BASTION("The Bastion"),
        THE_DUKEDOM("The Dukedom"),
        THE_WASTELAND("The Wasteland"),
        THRONE_ROOM("Throne Room"),
        // </editor-fold>

        // <editor-fold desc="Jerry's Workshop">
        EINARYS_EMPORIUM("Einary's Emporium"),
        GARYS_SHACK("Gary's Shack"),
        GLACIAL_CAVE("Glacial Cave"),
        HOT_SPRINGS("Hot Springs"),
        JERRY_POND("Jerry Pond"),
        JERRYS_WORKSHOP("Jerry's Workshop"),
        MOUNT_JERRY("Mount Jerry"),
        REFLECTIVE_POND("Reflective Pond"),
        SHERRYS_SHOWROOM("Sherry's Showroom"),
        SUNKEN_JERRY_POND("Sunken Jerry Pond"),
        TERRYS_SHACK("Terry's Shack"),
        // </editor-fold>

        // <editor-fold desc="Dungeon">
        THE_CATACOMBS_ENTRANCE("The Catacombs (Entrance)"),
        THE_CATACOMBS_F1("The Catacombs (F1)"),
        THE_CATACOMBS_F2("The Catacombs (F2)"),
        THE_CATACOMBS_F3("The Catacombs (F3)"),
        THE_CATACOMBS_F4("The Catacombs (F4)"),
        THE_CATACOMBS_F5("The Catacombs (F5)"),
        THE_CATACOMBS_F6("The Catacombs (F6)"),
        THE_CATACOMBS_F7("The Catacombs (F7)"),
        MASTER_MODE_M1("The Catacombs (M1)"),
        MASTER_MODE_M2("The Catacombs (M2)"),
        MASTER_MODE_M3("The Catacombs (M3)"),
        MASTER_MODE_M4("The Catacombs (M4)"),
        MASTER_MODE_M5("The Catacombs (M5)"),
        MASTER_MODE_M6("The Catacombs (M6)"),
        MASTER_MODE_M7("The Catacombs (M7)"),
        // </editor-fold>

        // <editor-fold desc="Rift Dimension">
        AROUND_COLOSSEUM("Around Colosseum"),
        BARRIER_STREET("Barrier Street"),
        BARRY_CENTER("Barry Center"),
        BARRY_HQ("Barry HQ"),
        BLACK_LAGOON("Black Lagoon"),
        BOOK_IN_A_BOOK("Book in a Book"),
        BROKEN_CAGE("Broken Cage"),
        CAKE_HOUSE("Cake House"),
        //        COLOSSEUM("Colosseum"), DUPLICATE
        DOLPHIN_TRAINER("Dolphin Trainer"),
        DREADFARM("Dreadfarm"),
        DÉJÀ_VU_ALLEY("Déjà Vu Alley"),
        EMPTY_BANK("Empty Bank"),
        ENIGMAS_CRIB("Enigma's Crib"),
        FAIRYLOSOPHER_TOWER("Fairylosopher Tower"),
        GREAT_BEANSTALK("Great Beanstalk"),
        HALF_EATEN_CAVE("Half-Eaten Cave"),
        INFESTED_HOUSE("Infested House"),
        LAGOON_CAVE("Lagoon Cave"),
        LAGOON_HUT("Lagoon Hut"),
        LEECHES_LAIR("Leeches Lair"),
        LIVING_CAVE("Living Cave"),
        LIVING_STILLNESS("Living Stillness"),
        LONELY_TERRACE("Lonely Terrace"),
        MIRRORVERSE("Mirrorverse"),
        MURDER_HOUSE("Murder House"),
        OTHERSIDE("Otherside"),
        OUBLIETTE("Oubliette"),
        PHOTON_PATHWAY("Photon Pathway"),
        PUMPGROTTO("Pumpgrotto"),
        RIFT_GALLERY_ENTRANCE("Rift Gallery Entrance"),
        RIFT_GALLERY("Rift Gallery"),
        SHIFTED_TAVERN("Shifted Tavern"),
        STILLGORE_CHÂTEAU("Stillgore Château"),
        TAYLORS("Taylor's"),
        //        THE_BASTION("The Bastion"), Duplicate
        VILLAGE_PLAZA("Village Plaza"),
        WEST_VILLAGE("West Village"),
        //        WIZARD_TOWER("Wizard Tower"), DUPLICATE
        YOUR_ISLAND("\"Your\" Island"),
        WYLD_WOODS("Wyld Woods");
        // </editor-fold>


        public final String name;

        SubLocation(String name) {
            this.name = name;
        }
    }

    @Override
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !this.canEnable()) return;
        List<String> scoreboard = ScoreboardUtils.getScoreboardLines();
        Collections.reverse(scoreboard);

        if (!scoreboard.isEmpty()) {
            outer:
            for (String s : scoreboard) {
                if (!(s.contains("⏣") || s.contains("ф"))) continue;
                for (SubLocation subLoc : SubLocation.values()) {
                    if (!ScoreboardUtils.cleanSB(s).contains(subLoc.name)) continue;
                    this.subLocation = subLoc;
                    break outer;
                }
            }
        }

        for (String line : TablistUtils.getTabList()) {
            if (!line.contains("Area")) continue;
            Matcher matcher = this.areaPattern.matcher(line);
            if (!matcher.find()) continue;
            String name = matcher.group(1);
            for (Location location : Location.values()) {
                if (name.contains(location.name)) {
                    this.location = location;
                    return;
                }
            }
        }

        if (ScoreboardUtils.getScoreboardLines().isEmpty()){
            this.location = Location.LIMBO;
            return;
        }
        if (!ScoreboardUtils.getScoreboardTitle().toLowerCase().contains("skyblock")) {
            this.location = Location.LOBBY;
        } else {
            this.location = Location.LIMBO;
        }
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    @Override
    public void onChatMessageReceive(ClientChatReceivedEvent event) {

    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        this.location = Location.NOWHERE;
        this.subLocation = SubLocation.NOWHERE;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.location = Location.NOWHERE;
        this.subLocation = SubLocation.NOWHERE;
    }
}
