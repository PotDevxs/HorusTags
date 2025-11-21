package dev.artix.horus.models;

import java.util.UUID;

public class PlayerAchievement {
    
    private UUID playerUUID;
    private String achievementId;
    private int progress;
    private boolean completed;
    private long completedAt;
    
    public PlayerAchievement(UUID playerUUID, String achievementId) {
        this.playerUUID = playerUUID;
        this.achievementId = achievementId;
        this.progress = 0;
        this.completed = false;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public String getAchievementId() {
        return achievementId;
    }
    
    public void setAchievementId(String achievementId) {
        this.achievementId = achievementId;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
    
    public void addProgress(int amount) {
        this.progress += amount;
    }
}

