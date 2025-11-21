package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.events.AchievementCompleteEvent;
import dev.artix.horus.models.Achievement;
import dev.artix.horus.models.PlayerAchievement;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementManager {
    
    private final Horus plugin;
    private final Map<String, Achievement> achievements;
    private final Map<UUID, Map<String, PlayerAchievement>> playerAchievements;
    
    public AchievementManager(Horus plugin) {
        this.plugin = plugin;
        this.achievements = new ConcurrentHashMap<>();
        this.playerAchievements = new ConcurrentHashMap<>();
    }
    
    public void loadAchievements() {
        achievements.clear();
        
        List<Achievement> allAchievements = plugin.getDatabaseManager().getAdapter().getAllAchievements();
        for (Achievement achievement : allAchievements) {
            if (achievement.isEnabled()) {
                achievements.put(achievement.getId(), achievement);
            }
        }
        
        LoggerUtil.info("Carregadas " + achievements.size() + " conquistas");
    }
    
    
    public void addProgress(UUID uuid, Achievement.AchievementType type, int amount) {
        for (Achievement achievement : achievements.values()) {
            if (achievement.getType() == type && achievement.isEnabled()) {
                addProgress(uuid, achievement.getId(), amount);
            }
        }
    }
    
    public void addProgress(UUID uuid, String achievementId, int amount) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null || !achievement.isEnabled()) {
            return;
        }
        
        PlayerAchievement playerAchievement = getPlayerAchievement(uuid, achievementId);
        if (playerAchievement.isCompleted()) {
            return;
        }
        
        playerAchievement.addProgress(amount);
        
        if (playerAchievement.getProgress() >= achievement.getRequiredValue()) {
            completeAchievement(uuid, achievement);
        } else {
            savePlayerAchievement(playerAchievement);
        }
    }
    
    private void completeAchievement(UUID uuid, Achievement achievement) {
        PlayerAchievement playerAchievement = getPlayerAchievement(uuid, achievement.getId());
        playerAchievement.setCompleted(true);
        playerAchievement.setCompletedAt(System.currentTimeMillis());
        
        savePlayerAchievement(playerAchievement);
        
        if (achievement.getTagReward() != null) {
            plugin.getPlayerTagManager().giveTag(uuid, achievement.getTagReward(), "ACHIEVEMENT");
        }
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            AchievementCompleteEvent event = new AchievementCompleteEvent(player, achievement);
            Bukkit.getPluginManager().callEvent(event);
            
            plugin.getNotificationManager().sendAchievementNotification(player, achievement);
        }
    }
    
    public PlayerAchievement getPlayerAchievement(UUID uuid, String achievementId) {
        Map<String, PlayerAchievement> achievements = playerAchievements.computeIfAbsent(uuid, k -> new HashMap<>());
        
        PlayerAchievement playerAchievement = achievements.get(achievementId);
        if (playerAchievement == null) {
            playerAchievement = loadPlayerAchievement(uuid, achievementId);
            if (playerAchievement == null) {
                playerAchievement = new PlayerAchievement(uuid, achievementId);
            }
            achievements.put(achievementId, playerAchievement);
        }
        
        return playerAchievement;
    }
    
    private PlayerAchievement loadPlayerAchievement(UUID uuid, String achievementId) {
        return plugin.getDatabaseManager().getAdapter().getPlayerAchievement(uuid, achievementId);
    }
    
    private void savePlayerAchievement(PlayerAchievement playerAchievement) {
        plugin.getDatabaseManager().getAdapter().savePlayerAchievement(playerAchievement);
    }
    
    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }
    
    public Collection<Achievement> getAllAchievements() {
        return achievements.values();
    }
    
    public List<PlayerAchievement> getPlayerAchievements(UUID uuid) {
        Map<String, PlayerAchievement> achievements = playerAchievements.get(uuid);
        if (achievements == null) {
            loadPlayerAchievements(uuid);
            achievements = playerAchievements.get(uuid);
        }
        return achievements != null ? new ArrayList<>(achievements.values()) : new ArrayList<>();
    }
    
    private void loadPlayerAchievements(UUID uuid) {
        Map<String, PlayerAchievement> achievements = new HashMap<>();
        
        List<PlayerAchievement> playerAchievementsList = plugin.getDatabaseManager().getAdapter().getPlayerAchievements(uuid);
        for (PlayerAchievement playerAchievement : playerAchievementsList) {
            achievements.put(playerAchievement.getAchievementId(), playerAchievement);
        }
        
        playerAchievements.put(uuid, achievements);
    }
}

