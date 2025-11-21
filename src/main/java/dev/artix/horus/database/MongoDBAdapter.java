package dev.artix.horus.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import dev.artix.horus.Horus;
import dev.artix.horus.models.Achievement;
import dev.artix.horus.models.PlayerAchievement;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.LoggerUtil;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class MongoDBAdapter implements DatabaseAdapter {
    
    private final Horus plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> tagsCollection;
    private MongoCollection<Document> playerTagsCollection;
    private MongoCollection<Document> achievementsCollection;
    private MongoCollection<Document> playerAchievementsCollection;
    private MongoCollection<Document> purchasesCollection;
    
    public MongoDBAdapter(Horus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        FileConfiguration dbConfig = plugin.getConfigManager().getDatabase();
        
        String connectionString = dbConfig.getString("mongodb.connection-string");
        String databaseName = dbConfig.getString("mongodb.database", "horus");
        
        if (connectionString == null || connectionString.isEmpty()) {
            String host = dbConfig.getString("mongodb.host", "localhost");
            int port = dbConfig.getInt("mongodb.port", 27017);
            connectionString = "mongodb://" + host + ":" + port;
        }
        
        try {
            MongoClientURI uri = new MongoClientURI(connectionString);
            mongoClient = new MongoClient(uri);
            database = mongoClient.getDatabase(databaseName);
            
            tagsCollection = database.getCollection("tags");
            playerTagsCollection = database.getCollection("player_tags");
            achievementsCollection = database.getCollection("achievements");
            playerAchievementsCollection = database.getCollection("player_achievements");
            purchasesCollection = database.getCollection("purchases");
            
            LoggerUtil.info("MongoDB conectado: " + databaseName);
        } catch (Exception e) {
            LoggerUtil.severe("Erro ao conectar ao MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    
    @Override
    public void saveTag(Tag tag) {
        Document doc = tagToDocument(tag);
        tagsCollection.replaceOne(Filters.eq("id", tag.getId()), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }
    
    @Override
    public Tag getTag(String id) {
        Document doc = tagsCollection.find(Filters.eq("id", id)).first();
        return doc != null ? documentToTag(doc) : null;
    }
    
    @Override
    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();
        for (Document doc : tagsCollection.find()) {
            Tag tag = documentToTag(doc);
            if (tag != null) {
                tags.add(tag);
            }
        }
        return tags;
    }
    
    @Override
    public void deleteTag(String id) {
        tagsCollection.deleteOne(Filters.eq("id", id));
    }
    
    private Document tagToDocument(Tag tag) {
        Document doc = new Document("id", tag.getId())
            .append("name", tag.getName())
            .append("display-name", tag.getDisplayName())
            .append("prefix", tag.getPrefix())
            .append("suffix", tag.getSuffix())
            .append("description", tag.getDescription())
            .append("category", tag.getCategory())
            .append("rarity", tag.getRarity() != null ? tag.getRarity().name() : null)
            .append("type", tag.getType() != null ? tag.getType().name() : null)
            .append("price", tag.getPrice())
            .append("permission", tag.getPermission())
            .append("required-groups", tag.getRequiredGroups() != null ? new ArrayList<>(tag.getRequiredGroups()) : null)
            .append("required-achievements", tag.getRequiredAchievements() != null ? new ArrayList<>(tag.getRequiredAchievements()) : null)
            .append("duration", tag.getDuration())
            .append("priority", tag.getPriority())
            .append("animated", tag.isAnimated())
            .append("animation-frames", tag.getAnimationFrames())
            .append("animation-speed", tag.getAnimationSpeed())
            .append("glow", tag.isGlow())
            .append("particle-effect", tag.getParticleEffect())
            .append("color", tag.getColor())
            .append("format", tag.getFormat())
            .append("limited", tag.isLimited())
            .append("max-owners", tag.getMaxOwners())
            .append("seasonal", tag.isSeasonal())
            .append("season", tag.getSeason())
            .append("purchasable", tag.isPurchasable())
            .append("tradeable", tag.isTradeable())
            .append("giftable", tag.isGiftable())
            .append("conditions", tag.getConditions() != null ? new ArrayList<>(tag.getConditions()) : null)
            .append("regions", tag.getRegions() != null ? new ArrayList<>(tag.getRegions()) : null)
            .append("time-restriction", tag.getTimeRestriction())
            .append("enabled", tag.isEnabled())
            .append("created-at", tag.getCreatedAt());
        
        return doc;
    }
    
    private Tag documentToTag(Document doc) {
        String id = doc.getString("id");
        if (id == null) return null;
        
        Tag tag = new Tag(id, doc.getString("name"));
        tag.setDisplayName(doc.getString("display-name"));
        tag.setPrefix(doc.getString("prefix"));
        tag.setSuffix(doc.getString("suffix"));
        tag.setDescription(doc.getList("description", String.class));
        tag.setCategory(doc.getString("category"));
        
        String rarity = doc.getString("rarity");
        if (rarity != null) {
            try {
                tag.setRarity(Tag.TagRarity.valueOf(rarity));
            } catch (Exception e) {}
        }
        
        String type = doc.getString("type");
        if (type != null) {
            try {
                tag.setType(Tag.TagType.valueOf(type));
            } catch (Exception e) {}
        }
        
        tag.setPrice(doc.getDouble("price") != null ? doc.getDouble("price") : 0.0);
        tag.setPermission(doc.getString("permission"));
        tag.setRequiredGroups(new HashSet<>(doc.getList("required-groups", String.class, new ArrayList<>())));
        tag.setRequiredAchievements(new HashSet<>(doc.getList("required-achievements", String.class, new ArrayList<>())));
        tag.setDuration(doc.getLong("duration") != null ? doc.getLong("duration") : -1);
        tag.setPriority(doc.getInteger("priority") != null ? doc.getInteger("priority") : 0);
        tag.setAnimated(doc.getBoolean("animated") != null ? doc.getBoolean("animated") : false);
        tag.setAnimationFrames(doc.getList("animation-frames", String.class));
        tag.setAnimationSpeed(doc.getLong("animation-speed") != null ? doc.getLong("animation-speed") : 1000);
        tag.setGlow(doc.getBoolean("glow") != null ? doc.getBoolean("glow") : false);
        tag.setParticleEffect(doc.getString("particle-effect"));
        tag.setColor(doc.getString("color"));
        tag.setFormat(doc.getString("format"));
        tag.setLimited(doc.getBoolean("limited") != null ? doc.getBoolean("limited") : false);
        tag.setMaxOwners(doc.getInteger("max-owners"));
        tag.setSeasonal(doc.getBoolean("seasonal") != null ? doc.getBoolean("seasonal") : false);
        tag.setSeason(doc.getString("season"));
        tag.setPurchasable(doc.getBoolean("purchasable") != null ? doc.getBoolean("purchasable") : false);
        tag.setTradeable(doc.getBoolean("tradeable") != null ? doc.getBoolean("tradeable") : false);
        tag.setGiftable(doc.getBoolean("giftable") != null ? doc.getBoolean("giftable") : false);
        tag.setConditions(new HashSet<>(doc.getList("conditions", String.class, new ArrayList<>())));
        tag.setRegions(new HashSet<>(doc.getList("regions", String.class, new ArrayList<>())));
        tag.setTimeRestriction(doc.getString("time-restriction"));
        tag.setEnabled(doc.getBoolean("enabled") != null ? doc.getBoolean("enabled") : true);
        tag.setCreatedAt(doc.getLong("created-at") != null ? doc.getLong("created-at") : System.currentTimeMillis());
        
        return tag;
    }
    
    @Override
    public void savePlayerTag(PlayerTag playerTag) {
        Document doc = new Document("player-uuid", playerTag.getPlayerUUID().toString())
            .append("tag-id", playerTag.getTagId())
            .append("obtained-at", playerTag.getObtainedAt())
            .append("expires-at", playerTag.getExpiresAt())
            .append("active", playerTag.isActive())
            .append("favorite", playerTag.isFavorite())
            .append("obtained-method", playerTag.getObtainedMethod());
        
        playerTagsCollection.replaceOne(
            Filters.and(
                Filters.eq("player-uuid", playerTag.getPlayerUUID().toString()),
                Filters.eq("tag-id", playerTag.getTagId())
            ),
            doc,
            new com.mongodb.client.model.ReplaceOptions().upsert(true)
        );
    }
    
    @Override
    public List<PlayerTag> getPlayerTags(UUID uuid) {
        List<PlayerTag> tags = new ArrayList<>();
        for (Document doc : playerTagsCollection.find(Filters.eq("player-uuid", uuid.toString()))) {
            PlayerTag playerTag = new PlayerTag(
                uuid,
                doc.getString("tag-id")
            );
            Long obtainedAt = doc.getLong("obtained-at");
            playerTag.setObtainedAt(obtainedAt != null ? obtainedAt : System.currentTimeMillis());
            Long expiresAt = doc.getLong("expires-at");
            playerTag.setExpiresAt(expiresAt != null ? expiresAt : 0L);
            Boolean active = doc.getBoolean("active");
            playerTag.setActive(active != null ? active : false);
            Boolean favorite = doc.getBoolean("favorite");
            playerTag.setFavorite(favorite != null ? favorite : false);
            String obtainedMethod = doc.getString("obtained-method");
            playerTag.setObtainedMethod(obtainedMethod != null ? obtainedMethod : "UNKNOWN");
            tags.add(playerTag);
        }
        return tags;
    }
    
    @Override
    public PlayerTag getPlayerTag(UUID uuid, String tagId) {
        Document doc = playerTagsCollection.find(
            Filters.and(
                Filters.eq("player-uuid", uuid.toString()),
                Filters.eq("tag-id", tagId)
            )
        ).first();
        
        if (doc == null) return null;
        
        PlayerTag playerTag = new PlayerTag(
            uuid,
            doc.getString("tag-id")
        );
        Long obtainedAt = doc.getLong("obtained-at");
        playerTag.setObtainedAt(obtainedAt != null ? obtainedAt : System.currentTimeMillis());
        Long expiresAt = doc.getLong("expires-at");
        playerTag.setExpiresAt(expiresAt != null ? expiresAt : 0L);
        Boolean active = doc.getBoolean("active");
        playerTag.setActive(active != null ? active : false);
        Boolean favorite = doc.getBoolean("favorite");
        playerTag.setFavorite(favorite != null ? favorite : false);
        String obtainedMethod = doc.getString("obtained-method");
        playerTag.setObtainedMethod(obtainedMethod != null ? obtainedMethod : "UNKNOWN");
        return playerTag;
    }
    
    @Override
    public void deletePlayerTag(UUID uuid, String tagId) {
        playerTagsCollection.deleteOne(
            Filters.and(
                Filters.eq("player-uuid", uuid.toString()),
                Filters.eq("tag-id", tagId)
            )
        );
    }
    
    @Override
    public void updatePlayerTagActive(UUID uuid, String tagId, boolean active) {
        playerTagsCollection.updateOne(
            Filters.and(
                Filters.eq("player-uuid", uuid.toString()),
                Filters.eq("tag-id", tagId)
            ),
            Updates.set("active", active)
        );
    }
    
    @Override
    public void updatePlayerTagFavorite(UUID uuid, String tagId, boolean favorite) {
        playerTagsCollection.updateOne(
            Filters.and(
                Filters.eq("player-uuid", uuid.toString()),
                Filters.eq("tag-id", tagId)
            ),
            Updates.set("favorite", favorite)
        );
    }
    
    @Override
    public void saveAchievement(Achievement achievement) {
        Document doc = new Document("id", achievement.getId())
            .append("name", achievement.getName())
            .append("description", achievement.getDescription())
            .append("tag-reward", achievement.getTagReward())
            .append("type", achievement.getType() != null ? achievement.getType().name() : null)
            .append("required-value", achievement.getRequiredValue())
            .append("required-tags", achievement.getRequiredTags() != null ? new ArrayList<>(achievement.getRequiredTags()) : null)
            .append("enabled", achievement.isEnabled())
            .append("created-at", achievement.getCreatedAt());
        
        achievementsCollection.replaceOne(Filters.eq("id", achievement.getId()), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }
    
    @Override
    public Achievement getAchievement(String id) {
        Document doc = achievementsCollection.find(Filters.eq("id", id)).first();
        return doc != null ? documentToAchievement(doc) : null;
    }
    
    @Override
    public List<Achievement> getAllAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        for (Document doc : achievementsCollection.find()) {
            Achievement achievement = documentToAchievement(doc);
            if (achievement != null) {
                achievements.add(achievement);
            }
        }
        return achievements;
    }
    
    @Override
    public void deleteAchievement(String id) {
        achievementsCollection.deleteOne(Filters.eq("id", id));
    }
    
    private Achievement documentToAchievement(Document doc) {
        String id = doc.getString("id");
        if (id == null) return null;
        
        Achievement achievement = new Achievement(id, doc.getString("name"));
        achievement.setDescription(doc.getString("description"));
        achievement.setTagReward(doc.getString("tag-reward"));
        
        String type = doc.getString("type");
        if (type != null) {
            try {
                achievement.setType(Achievement.AchievementType.valueOf(type));
            } catch (Exception e) {}
        }
        
        achievement.setRequiredValue(doc.getInteger("required-value") != null ? doc.getInteger("required-value") : 1);
        achievement.setRequiredTags(new HashSet<>(doc.getList("required-tags", String.class, new ArrayList<>())));
        achievement.setEnabled(doc.getBoolean("enabled") != null ? doc.getBoolean("enabled") : true);
        achievement.setCreatedAt(doc.getLong("created-at") != null ? doc.getLong("created-at") : System.currentTimeMillis());
        
        return achievement;
    }
    
    @Override
    public void savePlayerAchievement(PlayerAchievement playerAchievement) {
        Document doc = new Document("player-uuid", playerAchievement.getPlayerUUID().toString())
            .append("achievement-id", playerAchievement.getAchievementId())
            .append("progress", playerAchievement.getProgress())
            .append("completed", playerAchievement.isCompleted())
            .append("completed-at", playerAchievement.getCompletedAt());
        
        playerAchievementsCollection.replaceOne(
            Filters.and(
                Filters.eq("player-uuid", playerAchievement.getPlayerUUID().toString()),
                Filters.eq("achievement-id", playerAchievement.getAchievementId())
            ),
            doc,
            new com.mongodb.client.model.ReplaceOptions().upsert(true)
        );
    }
    
    @Override
    public PlayerAchievement getPlayerAchievement(UUID uuid, String achievementId) {
        Document doc = playerAchievementsCollection.find(
            Filters.and(
                Filters.eq("player-uuid", uuid.toString()),
                Filters.eq("achievement-id", achievementId)
            )
        ).first();
        
        if (doc == null) {
            return new PlayerAchievement(uuid, achievementId);
        }
        
        PlayerAchievement playerAchievement = new PlayerAchievement(uuid, achievementId);
        Integer progress = doc.getInteger("progress");
        playerAchievement.setProgress(progress != null ? progress : 0);
        Boolean completed = doc.getBoolean("completed");
        playerAchievement.setCompleted(completed != null ? completed : false);
        Long completedAt = doc.getLong("completed-at");
        playerAchievement.setCompletedAt(completedAt != null ? completedAt : 0L);
        return playerAchievement;
    }
    
    @Override
    public List<PlayerAchievement> getPlayerAchievements(UUID uuid) {
        List<PlayerAchievement> achievements = new ArrayList<>();
        for (Document doc : playerAchievementsCollection.find(Filters.eq("player-uuid", uuid.toString()))) {
            PlayerAchievement playerAchievement = new PlayerAchievement(
                uuid,
                doc.getString("achievement-id")
            );
            Integer progress = doc.getInteger("progress");
            playerAchievement.setProgress(progress != null ? progress : 0);
            Boolean completed = doc.getBoolean("completed");
            playerAchievement.setCompleted(completed != null ? completed : false);
            Long completedAt = doc.getLong("completed-at");
            playerAchievement.setCompletedAt(completedAt != null ? completedAt : 0L);
            achievements.add(playerAchievement);
        }
        return achievements;
    }
    
    @Override
    public void savePurchase(UUID uuid, String tagId, double price, long timestamp) {
        Document doc = new Document("player-uuid", uuid.toString())
            .append("tag-id", tagId)
            .append("price", price)
            .append("purchased-at", timestamp);
        
        purchasesCollection.insertOne(doc);
    }
    
    @Override
    public List<PurchaseRecord> getPlayerPurchases(UUID uuid) {
        List<PurchaseRecord> purchases = new ArrayList<>();
        for (Document doc : purchasesCollection.find(Filters.eq("player-uuid", uuid.toString()))) {
            PurchaseRecord record = new PurchaseRecord(
                uuid,
                doc.getString("tag-id"),
                doc.getDouble("price"),
                doc.getLong("purchased-at")
            );
            purchases.add(record);
        }
        return purchases;
    }
}



