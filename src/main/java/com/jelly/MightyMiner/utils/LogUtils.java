package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class LogUtils {
    static Minecraft mc = Minecraft.getMinecraft();
    private static String lastMessage = "";

    public static void addMessage(String message) {
        send("b§l" + message);
    }

    public static void addNote(String message) {
        send("e" + message);
    }

    public static void debugLog(String log) {
        if (!MightyMiner.config.debugLogMode) return;
        if(log.equalsIgnoreCase(lastMessage)) return;
        lastMessage = log;
        mc.thePlayer.addChatMessage(new ChatComponentText("§a[log]» §7" + log));
    }

    public static void logError(String message) {
        send("c" + message);
    }

    private static void send(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§9§lMightyMiner §r§8» §" + message));
    }
}
