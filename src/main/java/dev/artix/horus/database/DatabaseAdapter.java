package dev.artix.horus.database;

import dev.artix.horus.models.Achievement;
import dev.artix.horus.models.PlayerAchievement;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;

import java.util.List;
import java.util.UUID;

public interface DatabaseAdapter {
    
    void initialize();
    
    void close();
    
    // Tags
    void saveTag(Tag tag);
    Tag getTag(String id);
    List<Tag> getAllTags();
    void deleteTag(String id);
    
    // Player Tags
    void savePlayerTag(PlayerTag playerTag);
    List<PlayerTag> getPlayerTags(UUID uuid);
    PlayerTag getPlayerTag(UUID uuid, String tagId);
    void deletePlayerTag(UUID uuid, String tagId);
    void updatePlayerTagActive(UUID uuid, String tagId, boolean active);
    void updatePlayerTagFavorite(UUID uuid, String tagId, boolean favorite);
    
    // Achievements
    void saveAchievement(Achievement achievement);
    Achievement getAchievement(String id);
    List<Achievement> getAllAchievements();
    void deleteAchievement(String id);
    
    // Player Achievements
    void savePlayerAchievement(PlayerAchievement playerAchievement);
    PlayerAchievement getPlayerAchievement(UUID uuid, String achievementId);
    List<PlayerAchievement> getPlayerAchievements(UUID uuid);
    
    // Purchases
    void savePurchase(UUID uuid, String tagId, double price, long timestamp);
    List<PurchaseRecord> getPlayerPurchases(UUID uuid);
    
    class PurchaseRecord {
        private final UUID playerUUID;
        private final String tagId;
        private final double price;
        private final long purchasedAt;
        
        public PurchaseRecord(UUID playerUUID, String tagId, double price, long purchasedAt) {
            this.playerUUID = playerUUID;
            this.tagId = tagId;
            this.price = price;
            this.purchasedAt = purchasedAt;
        }
        
        public UUID getPlayerUUID() { return playerUUID; }
        public String getTagId() { return tagId; }
        public double getPrice() { return price; }
        public long getPurchasedAt() { return purchasedAt; }
    }
}



