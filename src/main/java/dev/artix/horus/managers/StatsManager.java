package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;

import java.util.*;

public class StatsManager {
    
    private final Horus plugin;
    
    public StatsManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public Map<String, Object> getTagStats(String tagId) {
        Map<String, Object> stats = new HashMap<>();
        
        int totalOwners = 0;
        int activeUsers = 0;
        
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            List<dev.artix.horus.models.PlayerTag> playerTags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
            for (dev.artix.horus.models.PlayerTag playerTag : playerTags) {
                if (playerTag.getTagId().equals(tagId) && !playerTag.isExpired()) {
                    totalOwners++;
                    if (playerTag.isActive()) {
                        activeUsers++;
                    }
                }
            }
        }
        
        stats.put("total-owners", totalOwners);
        stats.put("active-users", activeUsers);
        
        return stats;
    }
    
    public Map<String, Object> getPlayerStats(UUID uuid) {
        Map<String, Object> stats = new HashMap<>();
        
        List<dev.artix.horus.models.PlayerTag> tags = plugin.getPlayerTagManager().getPlayerTags(uuid);
        stats.put("total-tags", tags.size());
        stats.put("favorite-tags", plugin.getPlayerTagManager().getFavoriteTags(uuid).size());
        
        String activeTagId = plugin.getPlayerTagManager().getActiveTag(uuid);
        stats.put("active-tag", activeTagId != null ? activeTagId : "Nenhuma");
        
        List<dev.artix.horus.database.DatabaseAdapter.PurchaseRecord> purchases = 
            plugin.getDatabaseManager().getAdapter().getPlayerPurchases(uuid);
        stats.put("total-purchases", purchases.size());
        
        return stats;
    }
    
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total-tags", plugin.getTagManager().getAllTags().size());
        
        java.util.Set<String> categories = new java.util.HashSet<>();
        for (Tag tag : plugin.getTagManager().getAllTags()) {
            if (tag.getCategory() != null) {
                categories.add(tag.getCategory());
            }
        }
        stats.put("total-categories", categories.size());
        
        java.util.Set<java.util.UUID> uniquePlayers = new java.util.HashSet<>();
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            List<dev.artix.horus.models.PlayerTag> playerTags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
            if (!playerTags.isEmpty()) {
                uniquePlayers.add(player.getUniqueId());
            }
        }
        stats.put("total-players", uniquePlayers.size());
        
        int totalPurchases = 0;
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            totalPurchases += plugin.getDatabaseManager().getAdapter().getPlayerPurchases(player.getUniqueId()).size();
        }
        stats.put("total-purchases", totalPurchases);
        
        return stats;
    }
    
    public List<Tag> getMostPopularTags(int limit) {
        Map<String, Integer> tagCounts = new HashMap<>();
        
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            List<dev.artix.horus.models.PlayerTag> playerTags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
            for (dev.artix.horus.models.PlayerTag playerTag : playerTags) {
                if (!playerTag.isExpired()) {
                    tagCounts.put(playerTag.getTagId(), tagCounts.getOrDefault(playerTag.getTagId(), 0) + 1);
                }
            }
        }
        
        List<Tag> popularTags = new ArrayList<>();
        tagCounts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(limit)
            .forEach(entry -> {
                Tag tag = plugin.getTagManager().getTag(entry.getKey());
                if (tag != null) {
                    popularTags.add(tag);
                }
            });
        
        return popularTags;
    }
}

