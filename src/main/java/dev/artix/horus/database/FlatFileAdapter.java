package dev.artix.horus.database;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Achievement;
import dev.artix.horus.models.PlayerAchievement;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlatFileAdapter implements DatabaseAdapter {
    
    private final Horus plugin;
    private final File dataFolder;
    private final File tagsFolder;
    private final File playersFolder;
    private final File achievementsFolder;
    
    public FlatFileAdapter(Horus plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.tagsFolder = new File(dataFolder, "tags");
        this.playersFolder = new File(dataFolder, "players");
        this.achievementsFolder = new File(dataFolder, "achievements");
    }
    
    @Override
    public void initialize() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        if (!tagsFolder.exists()) {
            tagsFolder.mkdirs();
        }
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
        if (!achievementsFolder.exists()) {
            achievementsFolder.mkdirs();
        }
        LoggerUtil.info("Flat-File database inicializado");
    }
    
    @Override
    public void close() {
        // Flat-File não precisa fechar conexões
    }
    
    @Override
    public void saveTag(Tag tag) {
        File file = new File(tagsFolder, tag.getId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        config.set("id", tag.getId());
        config.set("name", tag.getName());
        config.set("display-name", tag.getDisplayName());
        config.set("prefix", tag.getPrefix());
        config.set("suffix", tag.getSuffix());
        config.set("description", tag.getDescription());
        config.set("category", tag.getCategory());
        config.set("rarity", tag.getRarity() != null ? tag.getRarity().name() : null);
        config.set("type", tag.getType() != null ? tag.getType().name() : null);
        config.set("price", tag.getPrice());
        config.set("permission", tag.getPermission());
        config.set("required-groups", tag.getRequiredGroups() != null ? new ArrayList<>(tag.getRequiredGroups()) : null);
        config.set("required-achievements", tag.getRequiredAchievements() != null ? new ArrayList<>(tag.getRequiredAchievements()) : null);
        config.set("duration", tag.getDuration());
        config.set("priority", tag.getPriority());
        config.set("animated", tag.isAnimated());
        config.set("animation-frames", tag.getAnimationFrames());
        config.set("animation-speed", tag.getAnimationSpeed());
        config.set("glow", tag.isGlow());
        config.set("particle-effect", tag.getParticleEffect());
        config.set("color", tag.getColor());
        config.set("format", tag.getFormat());
        config.set("limited", tag.isLimited());
        config.set("max-owners", tag.getMaxOwners());
        config.set("seasonal", tag.isSeasonal());
        config.set("season", tag.getSeason());
        config.set("purchasable", tag.isPurchasable());
        config.set("tradeable", tag.isTradeable());
        config.set("giftable", tag.isGiftable());
        config.set("conditions", tag.getConditions() != null ? new ArrayList<>(tag.getConditions()) : null);
        config.set("regions", tag.getRegions() != null ? new ArrayList<>(tag.getRegions()) : null);
        config.set("time-restriction", tag.getTimeRestriction());
        config.set("enabled", tag.isEnabled());
        config.set("created-at", tag.getCreatedAt());
        
        try {
            config.save(file);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao salvar tag: " + e.getMessage());
        }
    }
    
    @Override
    public Tag getTag(String id) {
        File file = new File(tagsFolder, id + ".yml");
        if (!file.exists()) {
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return loadTagFromConfig(config);
    }
    
    @Override
    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();
        File[] files = tagsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Tag tag = loadTagFromConfig(config);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        
        return tags;
    }
    
    @Override
    public void deleteTag(String id) {
        File file = new File(tagsFolder, id + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }
    
    private Tag loadTagFromConfig(YamlConfiguration config) {
        String id = config.getString("id");
        if (id == null) return null;
        
        Tag tag = new Tag(id, config.getString("name", id));
        tag.setDisplayName(config.getString("display-name"));
        tag.setPrefix(config.getString("prefix"));
        tag.setSuffix(config.getString("suffix"));
        tag.setDescription(config.getStringList("description"));
        tag.setCategory(config.getString("category"));
        
        String rarity = config.getString("rarity");
        if (rarity != null) {
            try {
                tag.setRarity(Tag.TagRarity.valueOf(rarity));
            } catch (Exception e) {}
        }
        
        String type = config.getString("type");
        if (type != null) {
            try {
                tag.setType(Tag.TagType.valueOf(type));
            } catch (Exception e) {}
        }
        
        tag.setPrice(config.getDouble("price", 0.0));
        tag.setPermission(config.getString("permission"));
        tag.setRequiredGroups(new HashSet<>(config.getStringList("required-groups")));
        tag.setRequiredAchievements(new HashSet<>(config.getStringList("required-achievements")));
        tag.setDuration(config.getLong("duration", -1));
        tag.setPriority(config.getInt("priority", 0));
        tag.setAnimated(config.getBoolean("animated", false));
        tag.setAnimationFrames(config.getStringList("animation-frames"));
        tag.setAnimationSpeed(config.getLong("animation-speed", 1000));
        tag.setGlow(config.getBoolean("glow", false));
        tag.setParticleEffect(config.getString("particle-effect"));
        tag.setColor(config.getString("color"));
        tag.setFormat(config.getString("format"));
        tag.setLimited(config.getBoolean("limited", false));
        tag.setMaxOwners(config.getInt("max-owners", -1));
        tag.setSeasonal(config.getBoolean("seasonal", false));
        tag.setSeason(config.getString("season"));
        tag.setPurchasable(config.getBoolean("purchasable", false));
        tag.setTradeable(config.getBoolean("tradeable", false));
        tag.setGiftable(config.getBoolean("giftable", false));
        tag.setConditions(new HashSet<>(config.getStringList("conditions")));
        tag.setRegions(new HashSet<>(config.getStringList("regions")));
        tag.setTimeRestriction(config.getString("time-restriction"));
        tag.setEnabled(config.getBoolean("enabled", true));
        tag.setCreatedAt(config.getLong("created-at", System.currentTimeMillis()));
        
        return tag;
    }
    
    @Override
    public void savePlayerTag(PlayerTag playerTag) {
        File playerFile = new File(playersFolder, playerTag.getPlayerUUID().toString() + ".yml");
        YamlConfiguration config = playerFile.exists() ? 
            YamlConfiguration.loadConfiguration(playerFile) : new YamlConfiguration();
        
        List<Map<?,?>> tags = config.getMapList("tags");
        if (tags == null) {
            tags = new ArrayList<>();
        }
        
        Map<String, Object> tagData = new HashMap<>();
        tagData.put("tag-id", playerTag.getTagId());
        tagData.put("obtained-at", playerTag.getObtainedAt());
        tagData.put("expires-at", playerTag.getExpiresAt());
        tagData.put("active", playerTag.isActive());
        tagData.put("favorite", playerTag.isFavorite());
        tagData.put("obtained-method", playerTag.getObtainedMethod());
        
        tags.removeIf(t -> t.get("tag-id").equals(playerTag.getTagId()));
        tags.add(tagData);
        
        config.set("tags", tags);
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao salvar player tag: " + e.getMessage());
        }
    }
    
    @Override
    public List<PlayerTag> getPlayerTags(UUID uuid) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return new ArrayList<>();
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> tags = config.getMapList("tags");
        List<PlayerTag> playerTags = new ArrayList<>();
        
        for (Map<?, ?> tagData : tags) {
            PlayerTag playerTag = new PlayerTag(
                uuid,
                (String) tagData.get("tag-id")
            );
            Object obtainedAtObj = tagData.get("obtained-at");
            playerTag.setObtainedAt(obtainedAtObj != null ? ((Number) obtainedAtObj).longValue() : System.currentTimeMillis());
            Object expiresAtObj = tagData.get("expires-at");
            playerTag.setExpiresAt(expiresAtObj != null ? ((Number) expiresAtObj).longValue() : 0L);
            Object activeObj = tagData.get("active");
            playerTag.setActive(activeObj != null ? (Boolean) activeObj : false);
            Object favoriteObj = tagData.get("favorite");
            playerTag.setFavorite(favoriteObj != null ? (Boolean) favoriteObj : false);
            Object obtainedMethodObj = tagData.get("obtained-method");
            playerTag.setObtainedMethod(obtainedMethodObj != null ? (String) obtainedMethodObj : "UNKNOWN");
            playerTags.add(playerTag);
        }
        
        return playerTags;
    }
    
    @Override
    public PlayerTag getPlayerTag(UUID uuid, String tagId) {
        List<PlayerTag> tags = getPlayerTags(uuid);
        return tags.stream()
            .filter(t -> t.getTagId().equals(tagId))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public void deletePlayerTag(UUID uuid, String tagId) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> tags = new ArrayList<>(config.getMapList("tags"));
        tags.removeIf(t -> t.get("tag-id").equals(tagId));
        
        config.set("tags", tags);
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao deletar player tag: " + e.getMessage());
        }
    }
    
    @Override
    public void updatePlayerTagActive(UUID uuid, String tagId, boolean active) {
        PlayerTag playerTag = getPlayerTag(uuid, tagId);
        if (playerTag != null) {
            playerTag.setActive(active);
            savePlayerTag(playerTag);
        }
    }
    
    @Override
    public void updatePlayerTagFavorite(UUID uuid, String tagId, boolean favorite) {
        PlayerTag playerTag = getPlayerTag(uuid, tagId);
        if (playerTag != null) {
            playerTag.setFavorite(favorite);
            savePlayerTag(playerTag);
        }
    }
    
    @Override
    public void saveAchievement(Achievement achievement) {
        File file = new File(achievementsFolder, achievement.getId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        config.set("id", achievement.getId());
        config.set("name", achievement.getName());
        config.set("description", achievement.getDescription());
        config.set("tag-reward", achievement.getTagReward());
        config.set("type", achievement.getType() != null ? achievement.getType().name() : null);
        config.set("required-value", achievement.getRequiredValue());
        config.set("required-tags", achievement.getRequiredTags() != null ? new ArrayList<>(achievement.getRequiredTags()) : null);
        config.set("enabled", achievement.isEnabled());
        config.set("created-at", achievement.getCreatedAt());
        
        try {
            config.save(file);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao salvar achievement: " + e.getMessage());
        }
    }
    
    @Override
    public Achievement getAchievement(String id) {
        File file = new File(achievementsFolder, id + ".yml");
        if (!file.exists()) {
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return loadAchievementFromConfig(config);
    }
    
    @Override
    public List<Achievement> getAllAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        File[] files = achievementsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Achievement achievement = loadAchievementFromConfig(config);
                if (achievement != null) {
                    achievements.add(achievement);
                }
            }
        }
        
        return achievements;
    }
    
    @Override
    public void deleteAchievement(String id) {
        File file = new File(achievementsFolder, id + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }
    
    private Achievement loadAchievementFromConfig(YamlConfiguration config) {
        String id = config.getString("id");
        if (id == null) return null;
        
        Achievement achievement = new Achievement(id, config.getString("name", id));
        achievement.setDescription(config.getString("description"));
        achievement.setTagReward(config.getString("tag-reward"));
        
        String type = config.getString("type");
        if (type != null) {
            try {
                achievement.setType(Achievement.AchievementType.valueOf(type));
            } catch (Exception e) {}
        }
        
        achievement.setRequiredValue(config.getInt("required-value", 1));
        achievement.setRequiredTags(new HashSet<>(config.getStringList("required-tags")));
        achievement.setEnabled(config.getBoolean("enabled", true));
        achievement.setCreatedAt(config.getLong("created-at", System.currentTimeMillis()));
        
        return achievement;
    }
    
    @Override
    public void savePlayerAchievement(PlayerAchievement playerAchievement) {
        File playerFile = new File(playersFolder, playerAchievement.getPlayerUUID().toString() + ".yml");
        YamlConfiguration config = playerFile.exists() ? 
            YamlConfiguration.loadConfiguration(playerFile) : new YamlConfiguration();
        
        List<Map<?,?>> achievements = config.getMapList("achievements");
        if (achievements == null) {
            achievements = new ArrayList<>();
        }
        
        Map<String, Object> achievementData = new HashMap<>();
        achievementData.put("achievement-id", playerAchievement.getAchievementId());
        achievementData.put("progress", playerAchievement.getProgress());
        achievementData.put("completed", playerAchievement.isCompleted());
        achievementData.put("completed-at", playerAchievement.getCompletedAt());
        
        achievements.removeIf(a -> a.get("achievement-id").equals(playerAchievement.getAchievementId()));
        achievements.add(achievementData);
        
        config.set("achievements", achievements);
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao salvar player achievement: " + e.getMessage());
        }
    }
    
    @Override
    public PlayerAchievement getPlayerAchievement(UUID uuid, String achievementId) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return new PlayerAchievement(uuid, achievementId);
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> achievements = config.getMapList("achievements");
        
        for (Map<?, ?> achievementData : achievements) {
            if (achievementId.equals(achievementData.get("achievement-id"))) {
                PlayerAchievement playerAchievement = new PlayerAchievement(uuid, achievementId);
                Object progressObj = achievementData.get("progress");
                playerAchievement.setProgress(progressObj != null ? ((Number) progressObj).intValue() : 0);
                Object completedObj = achievementData.get("completed");
                playerAchievement.setCompleted(completedObj != null ? (Boolean) completedObj : false);
                Object completedAtObj = achievementData.get("completed-at");
                playerAchievement.setCompletedAt(completedAtObj != null ? ((Number) completedAtObj).longValue() : 0L);
                return playerAchievement;
            }
        }
        
        return new PlayerAchievement(uuid, achievementId);
    }
    
    @Override
    public List<PlayerAchievement> getPlayerAchievements(UUID uuid) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return new ArrayList<>();
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> achievements = config.getMapList("achievements");
        List<PlayerAchievement> playerAchievements = new ArrayList<>();
        
        for (Map<?, ?> achievementData : achievements) {
            PlayerAchievement playerAchievement = new PlayerAchievement(
                uuid,
                (String) achievementData.get("achievement-id")
            );
            Object progressObj = achievementData.get("progress");
            playerAchievement.setProgress(progressObj != null ? ((Number) progressObj).intValue() : 0);
            Object completedObj = achievementData.get("completed");
            playerAchievement.setCompleted(completedObj != null ? (Boolean) completedObj : false);
            Object completedAtObj = achievementData.get("completed-at");
            playerAchievement.setCompletedAt(completedAtObj != null ? ((Number) completedAtObj).longValue() : 0L);
            playerAchievements.add(playerAchievement);
        }
        
        return playerAchievements;
    }
    
    @Override
    public void savePurchase(UUID uuid, String tagId, double price, long timestamp) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        YamlConfiguration config = playerFile.exists() ? 
            YamlConfiguration.loadConfiguration(playerFile) : new YamlConfiguration();
        
        List<Map<?,?>> purchases = config.getMapList("purchases");
        if (purchases == null) {
            purchases = new ArrayList<>();
        }
        
        Map<String, Object> purchaseData = new HashMap<>();
        purchaseData.put("tag-id", tagId);
        purchaseData.put("price", price);
        purchaseData.put("purchased-at", timestamp);
        
        purchases.add(purchaseData);
        
        config.set("purchases", purchases);
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao salvar purchase: " + e.getMessage());
        }
    }
    
    @Override
    public List<PurchaseRecord> getPlayerPurchases(UUID uuid) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return new ArrayList<>();
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> purchases = config.getMapList("purchases");
        List<PurchaseRecord> purchaseRecords = new ArrayList<>();
        
        for (Map<?, ?> purchaseData : purchases) {
            PurchaseRecord record = new PurchaseRecord(
                uuid,
                (String) purchaseData.get("tag-id"),
                ((Number) purchaseData.get("price")).doubleValue(),
                ((Number) purchaseData.get("purchased-at")).longValue()
            );
            purchaseRecords.add(record);
        }
        
        return purchaseRecords;
    }
}



