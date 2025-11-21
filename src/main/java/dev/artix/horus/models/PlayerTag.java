package dev.artix.horus.models;

import java.util.UUID;

public class PlayerTag {
    
    private UUID playerUUID;
    private String tagId;
    private long obtainedAt;
    private long expiresAt;
    private boolean active;
    private boolean favorite;
    private String obtainedMethod;
    
    public PlayerTag(UUID playerUUID, String tagId) {
        this.playerUUID = playerUUID;
        this.tagId = tagId;
        this.obtainedAt = System.currentTimeMillis();
        this.active = false;
        this.favorite = false;
        this.obtainedMethod = "UNKNOWN";
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public String getTagId() {
        return tagId;
    }
    
    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
    
    public long getObtainedAt() {
        return obtainedAt;
    }
    
    public void setObtainedAt(long obtainedAt) {
        this.obtainedAt = obtainedAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isFavorite() {
        return favorite;
    }
    
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
    
    public String getObtainedMethod() {
        return obtainedMethod;
    }
    
    public void setObtainedMethod(String obtainedMethod) {
        this.obtainedMethod = obtainedMethod;
    }
    
    public boolean isExpired() {
        if (expiresAt <= 0) return false;
        return System.currentTimeMillis() > expiresAt;
    }
    
    public long getTimeRemaining() {
        if (expiresAt <= 0) return -1;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }
}

