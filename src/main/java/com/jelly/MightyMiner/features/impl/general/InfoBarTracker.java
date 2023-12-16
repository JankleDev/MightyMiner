package com.jelly.MightyMiner.features.impl.general;

import com.jelly.MightyMiner.events.ScoreboardUpdateEvent;
import com.jelly.MightyMiner.features.AbstractFeature;
import com.jelly.MightyMiner.utils.LogUtils;
import lombok.Getter;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoBarTracker extends AbstractFeature {
    private static InfoBarTracker instance = null;

    public static InfoBarTracker getInstance() {
        if (instance == null) {
            instance = new InfoBarTracker();
        }
        return instance;
    }

    private Pattern patternMana = Pattern.compile("(\\d*)/(\\d*)✎");
    private Pattern patternHealth = Pattern.compile("(\\d*)/(\\d*)");
    @Getter
    private int currentMana = 0;
    @Getter
    private int maxMana = 0;

    @Getter
    private int currentHealth = 0;
    @Getter
    private int maxHealth = 0;

    @Override
    public String getFeatureName() {
        return "InfoBarTracker";
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
        return true;
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {

    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

    }

    public float getHealthPercentage() {
        if (this.maxHealth == 0) return 0;
        return ((float) this.currentHealth / this.maxHealth ) * 100;
    }

    public float getManaPercentage() {
        if (this.maxMana == 0) return 0;
        return ((float) this.currentMana / this.maxMana) * 100;
    }

    @Override
    @SubscribeEvent
    public void onChatMessageReceive(ClientChatReceivedEvent event) {
        if (event.type != 2) return;
        if (locationTracker.getLocation() == LocationTracker.Location.LIMBO ||
            locationTracker.getLocation() == LocationTracker.Location.LOBBY) return;

        String infoBar = StringUtils.stripControlCodes(event.message.getUnformattedText()).replace(",","");
        Matcher manaMatcher = patternMana.matcher(infoBar);
        Matcher healthMatcher = patternHealth.matcher(infoBar);

        if (infoBar.contains("NOT ENOUGH MANA")) {
            this.currentMana = 0;
            this.maxMana = 0;
        } else if (manaMatcher.find()) {
            this.currentMana = Integer.parseInt(manaMatcher.group(1).replace(",", ""));
            this.maxMana = Integer.parseInt(manaMatcher.group(2).replace(",", ""));
        }

        if (healthMatcher.find()) {
            this.currentHealth = Integer.parseInt(healthMatcher.group(1));
            this.maxHealth = Integer.parseInt(healthMatcher.group(2));
        }
    }
}
