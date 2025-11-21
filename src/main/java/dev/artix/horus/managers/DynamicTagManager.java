package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DynamicTagManager implements Listener {
    
    private final Horus plugin;
    private final Map<String, List<String>> dynamicTagRules;
    private final Map<UUID, String> playerStats;
    
    public DynamicTagManager(Horus plugin) {
        this.plugin = plugin;
        this.dynamicTagRules = new HashMap<>();
        this.playerStats = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startStatsTracking();
    }
    
    public void registerDynamicTag(String tagId, List<String> rules) {
        dynamicTagRules.put(tagId, rules);
    }
    
    public void updateDynamicTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
            if (activeTagId == null) continue;
            
            Tag tag = plugin.getTagManager().getTag(activeTagId);
            if (tag == null || !tag.isAnimated()) continue;
            
            if (dynamicTagRules.containsKey(activeTagId)) {
                List<String> rules = dynamicTagRules.get(activeTagId);
                if (shouldChangeTag(player, rules)) {
                    String newTagId = getDynamicTag(player, rules);
                    if (newTagId != null && !newTagId.equals(activeTagId)) {
                        plugin.getPlayerTagManager().setActiveTag(player.getUniqueId(), newTagId);
                    }
                }
            }
        }
    }
    
    private boolean shouldChangeTag(Player player, List<String> rules) {
        for (String rule : rules) {
            if (rule.startsWith("EVENT:")) {
                String eventType = rule.substring(6);
                if (checkEventCondition(player, eventType)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkEventCondition(Player player, String eventType) {
        switch (eventType.toUpperCase()) {
            case "KILL":
                return playerStats.containsKey(player.getUniqueId()) && 
                       playerStats.get(player.getUniqueId()).contains("KILL");
            case "DEATH":
                return playerStats.containsKey(player.getUniqueId()) && 
                       playerStats.get(player.getUniqueId()).contains("DEATH");
            case "PVP":
                return playerStats.containsKey(player.getUniqueId()) && 
                       playerStats.get(player.getUniqueId()).contains("PVP");
            default:
                return false;
        }
    }
    
    private String getDynamicTag(Player player, List<String> rules) {
        for (String rule : rules) {
            if (rule.startsWith("TAG:")) {
                String tagId = rule.substring(4);
                if (plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tagId)) {
                    return tagId;
                }
            }
        }
        return null;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        
        if (killer != null) {
            updateStat(killer.getUniqueId(), "KILL");
            updateStat(killer.getUniqueId(), "PVP");
            updateStat(player.getUniqueId(), "DEATH");
        } else {
            updateStat(player.getUniqueId(), "DEATH");
        }
    }
    
    private void updateStat(UUID uuid, String stat) {
        String current = playerStats.getOrDefault(uuid, "");
        playerStats.put(uuid, current + ":" + stat);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                String stats = playerStats.get(uuid);
                if (stats != null) {
                    stats = stats.replace(":" + stat, "");
                    if (stats.isEmpty()) {
                        playerStats.remove(uuid);
                    } else {
                        playerStats.put(uuid, stats);
                    }
                }
            }
        }.runTaskLater(plugin, 100L);
    }
    
    private void startStatsTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateDynamicTags();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}

