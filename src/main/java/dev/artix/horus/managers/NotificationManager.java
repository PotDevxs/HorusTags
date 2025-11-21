package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Achievement;
import dev.artix.horus.models.Tag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class NotificationManager {
    
    private final Horus plugin;
    
    public NotificationManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void sendTagObtainedNotification(Player player, Tag tag) {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        String message = messages.getString("notifications.tag-obtained", "&aVocê obteve a tag: &f{tag}");
        message = message.replace("{tag}", tag.getDisplayName());
        player.sendMessage(colorize(message));
    }
    
    public void sendTagExpiringNotification(Player player, Tag tag, long timeRemaining) {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        String message = messages.getString("notifications.tag-expiring", "&eSua tag {tag} expira em {time}");
        message = message.replace("{tag}", tag.getDisplayName());
        message = message.replace("{time}", formatTime(timeRemaining));
        player.sendMessage(colorize(message));
    }
    
    public void sendAchievementNotification(Player player, Achievement achievement) {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        String message = messages.getString("notifications.achievement-complete", "&6&lConquista desbloqueada: &f{achievement}");
        message = message.replace("{achievement}", achievement.getName());
        player.sendMessage(colorize(message));
        
        if (achievement.getTagReward() != null) {
            Tag tag = plugin.getTagManager().getTag(achievement.getTagReward());
            if (tag != null) {
                sendTagObtainedNotification(player, tag);
            }
        }
    }
    
    public void sendNewTagAvailableNotification(Player player, Tag tag) {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        String message = messages.getString("notifications.new-tag-available", "&bNova tag disponível: &f{tag}");
        message = message.replace("{tag}", tag.getDisplayName());
        player.sendMessage(colorize(message));
    }
    
    private String colorize(String message) {
        return message.replace("&", "§");
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}

