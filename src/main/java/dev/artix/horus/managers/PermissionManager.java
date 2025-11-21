package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.integrations.LuckPermsIntegration;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;

import java.util.Set;

public class PermissionManager {
    
    private final Horus plugin;
    private LuckPermsIntegration luckPermsIntegration;
    
    public PermissionManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void setLuckPermsIntegration(LuckPermsIntegration luckPermsIntegration) {
        this.luckPermsIntegration = luckPermsIntegration;
    }
    
    public boolean canUseTag(Player player, Tag tag) {
        if (tag.getPermission() != null && !player.hasPermission(tag.getPermission())) {
            return false;
        }
        
        if (tag.getRequiredGroups() != null && !tag.getRequiredGroups().isEmpty()) {
            if (luckPermsIntegration == null || !luckPermsIntegration.isEnabled()) {
                return false;
            }
            
            Set<String> playerGroups = luckPermsIntegration.getPlayerGroups(player);
            boolean hasRequiredGroup = false;
            
            for (String requiredGroup : tag.getRequiredGroups()) {
                if (playerGroups.contains(requiredGroup)) {
                    hasRequiredGroup = true;
                    break;
                }
            }
            
            if (!hasRequiredGroup) {
                return false;
            }
        }
        
        if (tag.getRequiredAchievements() != null && !tag.getRequiredAchievements().isEmpty()) {
            for (String achievementId : tag.getRequiredAchievements()) {
                if (!plugin.getAchievementManager().getPlayerAchievement(player.getUniqueId(), achievementId).isCompleted()) {
                    return false;
                }
            }
        }
        
        if (tag.isLimited() && tag.getMaxOwners() > 0) {
            int ownerCount = getTagOwnerCount(tag.getId());
            if (ownerCount >= tag.getMaxOwners()) {
                return false;
            }
        }
        
        if (tag.isSeasonal() && tag.getSeason() != null) {
            if (!isCurrentSeason(tag.getSeason())) {
                return false;
            }
        }
        
        if (!plugin.getConditionManager().checkConditions(player, tag)) {
            return false;
        }
        
        if (!plugin.getConditionManager().checkRegions(player, tag)) {
            return false;
        }
        
        if (!plugin.getConditionManager().checkTimeRestriction(tag)) {
            return false;
        }
        
        return true;
    }
    
    private int getTagOwnerCount(String tagId) {
        int count = 0;
        for (dev.artix.horus.models.Tag tag : plugin.getTagManager().getAllTags()) {
            if (tag.getId().equals(tagId)) {
                java.util.List<dev.artix.horus.models.PlayerTag> allPlayerTags = new java.util.ArrayList<>();
                for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    allPlayerTags.addAll(plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId()));
                }
                count = (int) allPlayerTags.stream()
                        .filter(pt -> pt.getTagId().equals(tagId))
                        .count();
                break;
            }
        }
        return count;
    }
    
    private boolean isCurrentSeason(String season) {
        String currentSeason = getCurrentSeason();
        return currentSeason != null && currentSeason.equalsIgnoreCase(season);
    }
    
    private String getCurrentSeason() {
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1;
        
        if (month >= 12 || month <= 2) {
            return "WINTER";
        } else if (month >= 3 && month <= 5) {
            return "SPRING";
        } else if (month >= 6 && month <= 8) {
            return "SUMMER";
        } else {
            return "AUTUMN";
        }
    }
    
    public boolean hasTagLimit(Player player) {
        int limit = plugin.getConfigManager().getConfig().getInt("tag-limit", -1);
        if (limit <= 0) {
            return false;
        }
        
        int playerTagCount = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId()).size();
        return playerTagCount >= limit;
    }
    
    public int getTagLimit(Player player) {
        int defaultLimit = plugin.getConfigManager().getConfig().getInt("tag-limit", -1);
        
        if (luckPermsIntegration != null && luckPermsIntegration.isEnabled()) {
            String limitPermission = "horus.tag.limit.";
            for (int i = 100; i >= 1; i--) {
                if (player.hasPermission(limitPermission + i)) {
                    return i;
                }
            }
        }
        
        return defaultLimit;
    }
}

