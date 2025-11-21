package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class LogManager {
    
    private final Horus plugin;
    private final File logFile;
    
    public LogManager(Horus plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "logs" + File.separator + "actions.log");
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
    }
    
    private void log(String action, String details) {
        if (!plugin.getConfigManager().getConfig().getBoolean("logging.enabled", true)) {
            return;
        }
        
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.println("[" + timestamp + "] " + action + " - " + details);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao escrever no log: " + e.getMessage());
        }
    }
    
    public void logTagCreate(UUID adminUUID, String tagId) {
        String adminName = getPlayerName(adminUUID);
        log("TAG_CREATE", "Admin: " + adminName + ", Tag: " + tagId);
    }
    
    public void logTagDelete(UUID adminUUID, String tagId) {
        String adminName = getPlayerName(adminUUID);
        log("TAG_DELETE", "Admin: " + adminName + ", Tag: " + tagId);
    }
    
    public void logTagGive(UUID adminUUID, UUID targetUUID, String tagId) {
        String adminName = getPlayerName(adminUUID);
        String targetName = getPlayerName(targetUUID);
        log("TAG_GIVE", "Admin: " + adminName + ", Target: " + targetName + ", Tag: " + tagId);
    }
    
    public void logTagPurchase(UUID playerUUID, String tagId, double price) {
        String playerName = getPlayerName(playerUUID);
        log("TAG_PURCHASE", "Player: " + playerName + ", Tag: " + tagId + ", Price: " + price);
    }
    
    public void logTrade(UUID senderUUID, UUID targetUUID, String senderTagId, String targetTagId) {
        String senderName = getPlayerName(senderUUID);
        String targetName = getPlayerName(targetUUID);
        log("TAG_TRADE", "Sender: " + senderName + ", Target: " + targetName + ", SenderTag: " + senderTagId + ", TargetTag: " + targetTagId);
    }
    
    public void logGift(UUID senderUUID, UUID targetUUID, String tagId) {
        String senderName = getPlayerName(senderUUID);
        String targetName = getPlayerName(targetUUID);
        log("TAG_GIFT", "Sender: " + senderName + ", Target: " + targetName + ", Tag: " + tagId);
    }
    
    public void logTagEquip(UUID playerUUID, String tagId) {
        String playerName = getPlayerName(playerUUID);
        log("TAG_EQUIP", "Player: " + playerName + ", Tag: " + tagId);
    }
    
    private String getPlayerName(UUID uuid) {
        org.bukkit.OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        return player.getName() != null ? player.getName() : uuid.toString();
    }
}

