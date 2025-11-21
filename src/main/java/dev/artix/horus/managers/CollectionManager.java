package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.entity.Player;

import java.util.*;

public class CollectionManager {
    
    private final Horus plugin;
    private final Map<String, Collection> collections;
    
    public CollectionManager(Horus plugin) {
        this.plugin = plugin;
        this.collections = new HashMap<>();
    }
    
    public void registerCollection(String id, String name, List<String> requiredTags, String rewardTag) {
        Collection collection = new Collection(id, name, requiredTags, rewardTag);
        collections.put(id, collection);
    }
    
    public Collection getCollection(String id) {
        return collections.get(id);
    }
    
    public List<Collection> getPlayerCollections(UUID uuid) {
        List<Collection> playerCollections = new ArrayList<>();
        
        for (Collection collection : collections.values()) {
            CollectionProgress progress = getCollectionProgress(uuid, collection);
            playerCollections.add(collection);
        }
        
        return playerCollections;
    }
    
    public CollectionProgress getCollectionProgress(UUID uuid, Collection collection) {
        int collected = 0;
        List<String> playerTags = new ArrayList<>();
        
        for (String tagId : collection.getRequiredTags()) {
            if (plugin.getPlayerTagManager().hasTag(uuid, tagId)) {
                collected++;
                playerTags.add(tagId);
            }
        }
        
        boolean completed = collected >= collection.getRequiredTags().size();
        
        return new CollectionProgress(collection, collected, collection.getRequiredTags().size(), completed, playerTags);
    }
    
    public void checkAndRewardCollection(UUID uuid, String tagId) {
        for (Collection collection : collections.values()) {
            if (collection.getRequiredTags().contains(tagId)) {
                CollectionProgress progress = getCollectionProgress(uuid, collection);
                
                if (progress.isCompleted() && collection.getRewardTag() != null) {
                    if (!plugin.getPlayerTagManager().hasTag(uuid, collection.getRewardTag())) {
                        plugin.getPlayerTagManager().giveTag(uuid, collection.getRewardTag(), "COLLECTION");
                        
                        Player player = org.bukkit.Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.sendMessage(ColorUtil.translateColors("&6&lColeção Completa!"));
                            player.sendMessage(ColorUtil.translateColors("&7Você completou a coleção: &f" + collection.getName()));
                            
                            Tag rewardTag = plugin.getTagManager().getTag(collection.getRewardTag());
                            if (rewardTag != null) {
                                player.sendMessage(ColorUtil.translateColors("&7Recompensa: &f" + rewardTag.getDisplayName()));
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static class Collection {
        private final String id;
        private final String name;
        private final List<String> requiredTags;
        private final String rewardTag;
        
        public Collection(String id, String name, List<String> requiredTags, String rewardTag) {
            this.id = id;
            this.name = name;
            this.requiredTags = requiredTags;
            this.rewardTag = rewardTag;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public List<String> getRequiredTags() {
            return requiredTags;
        }
        
        public String getRewardTag() {
            return rewardTag;
        }
    }
    
    public static class CollectionProgress {
        private final Collection collection;
        private final int collected;
        private final int total;
        private final boolean completed;
        private final List<String> playerTags;
        
        public CollectionProgress(Collection collection, int collected, int total, boolean completed, List<String> playerTags) {
            this.collection = collection;
            this.collected = collected;
            this.total = total;
            this.completed = completed;
            this.playerTags = playerTags;
        }
        
        public Collection getCollection() {
            return collection;
        }
        
        public int getCollected() {
            return collected;
        }
        
        public int getTotal() {
            return total;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public List<String> getPlayerTags() {
            return playerTags;
        }
        
        public double getProgressPercentage() {
            return (double) collected / total * 100;
        }
    }
}

