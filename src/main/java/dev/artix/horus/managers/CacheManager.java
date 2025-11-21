package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    
    private final Horus plugin;
    private final Map<UUID, Long> playerCache;
    private final Map<String, Tag> tagCache;
    private final long cacheExpiry;
    
    public CacheManager(Horus plugin) {
        this.plugin = plugin;
        this.playerCache = new ConcurrentHashMap<>();
        this.tagCache = new ConcurrentHashMap<>();
        this.cacheExpiry = plugin.getConfigManager().getConfig().getLong("cache.expiry", 300000);
    }
    
    public void cachePlayer(UUID uuid) {
        playerCache.put(uuid, System.currentTimeMillis());
    }
    
    public boolean isPlayerCached(UUID uuid) {
        Long cached = playerCache.get(uuid);
        if (cached == null) {
            return false;
        }
        
        if (System.currentTimeMillis() - cached > cacheExpiry) {
            playerCache.remove(uuid);
            return false;
        }
        
        return true;
    }
    
    public void cacheTag(Tag tag) {
        tagCache.put(tag.getId(), tag);
    }
    
    public Tag getCachedTag(String id) {
        return tagCache.get(id);
    }
    
    public void clearPlayerCache(UUID uuid) {
        playerCache.remove(uuid);
    }
    
    public void clearTagCache(String id) {
        tagCache.remove(id);
    }
    
    public void clearAllCache() {
        playerCache.clear();
        tagCache.clear();
    }
    
    public void saveCache() {
        clearExpiredCache();
    }
    
    private void clearExpiredCache() {
        long currentTime = System.currentTimeMillis();
        playerCache.entrySet().removeIf(entry -> currentTime - entry.getValue() > cacheExpiry);
    }
}

