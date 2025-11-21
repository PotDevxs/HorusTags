package dev.artix.horus.utils;

import dev.artix.horus.Horus;
import org.bukkit.ChatColor;

public class LoggerUtil {
    
    private static final String PREFIX = ChatColor.GOLD + "[Horus] " + ChatColor.RESET;
    
    public static void info(String message) {
        Horus.getInstance().getLogger().info(PREFIX + message);
    }
    
    public static void warning(String message) {
        Horus.getInstance().getLogger().warning(PREFIX + message);
    }
    
    public static void severe(String message) {
        Horus.getInstance().getLogger().severe(PREFIX + message);
    }
    
    public static void debug(String message) {
        if (Horus.getInstance().getConfigManager().getConfig().getBoolean("debug", false)) {
            Horus.getInstance().getLogger().info(PREFIX + "[DEBUG] " + message);
        }
    }
}

