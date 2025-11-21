package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.events.TagChangeEvent;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTagManager {
    
    private final Horus plugin;
    private final Map<UUID, String> activeTags;
    private final Map<UUID, List<PlayerTag>> playerTags;
    private final Map<UUID, Long> cooldowns;
    
    public PlayerTagManager(Horus plugin) {
        this.plugin = plugin;
        this.activeTags = new ConcurrentHashMap<>();
        this.playerTags = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
    }
    
    public void loadPlayerTags(UUID uuid) {
        List<PlayerTag> tags = plugin.getDatabaseManager().getAdapter().getPlayerTags(uuid);
        
        for (PlayerTag playerTag : tags) {
            if (playerTag.isActive() && !playerTag.isExpired()) {
                activeTags.put(uuid, playerTag.getTagId());
            }
        }
        
        playerTags.put(uuid, tags);
    }
    
    public boolean hasTag(UUID uuid, String tagId) {
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags == null) {
            loadPlayerTags(uuid);
            tags = playerTags.get(uuid);
        }
        
        if (tags == null) return false;
        
        for (PlayerTag playerTag : tags) {
            if (playerTag.getTagId().equals(tagId) && !playerTag.isExpired()) {
                return true;
            }
        }
        return false;
    }
    
    public void giveTag(UUID uuid, String tagId, String method) {
        if (hasTag(uuid, tagId)) {
            return;
        }
        
        Tag tag = plugin.getTagManager().getTag(tagId);
        if (tag == null || !tag.isEnabled()) {
            return;
        }
        
        int tagLimit = plugin.getConfigManager().getConfig().getInt("tag-limit", -1);
        if (tagLimit > 0) {
            List<PlayerTag> currentTags = getPlayerTags(uuid);
            long nonExpiredCount = currentTags.stream()
                    .filter(pt -> !pt.isExpired())
                    .count();
            
            if (nonExpiredCount >= tagLimit) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(plugin.getConfigManager().getMessages().getString("errors.tag-limit-reached", "&cVocê atingiu o limite de tags."));
                }
                return;
            }
        }
        
        PlayerTag playerTag = new PlayerTag(uuid, tagId);
        playerTag.setObtainedMethod(method);
        
        if (tag.getDuration() > 0) {
            playerTag.setExpiresAt(System.currentTimeMillis() + tag.getDuration());
        }
        
        List<PlayerTag> tags = playerTags.computeIfAbsent(uuid, k -> new ArrayList<>());
        tags.add(playerTag);
        
        savePlayerTag(playerTag);
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.getNotificationManager().sendTagObtainedNotification(player, tag);
            plugin.getCollectionManager().checkAndRewardCollection(uuid, tagId);
        } else {
            plugin.getCollectionManager().checkAndRewardCollection(uuid, tagId);
        }
    }
    
    public void removeTag(UUID uuid, String tagId) {
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags == null) return;
        
        tags.removeIf(pt -> pt.getTagId().equals(tagId));
        
        if (activeTags.get(uuid) != null && activeTags.get(uuid).equals(tagId)) {
            activeTags.remove(uuid);
        }
        
        deletePlayerTag(uuid, tagId);
    }
    
    public void setActiveTag(UUID uuid, String tagId) {
        if (!hasTag(uuid, tagId)) {
            return;
        }
        
        Player player = Bukkit.getPlayer(uuid);
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (player != null && tag != null) {
            if (!plugin.getConditionManager().checkConditions(player, tag)) {
                player.sendMessage(plugin.getConfigManager().getMessages().getString("errors.conditions-not-met", "&cVocê não atende às condições necessárias para usar esta tag."));
                return;
            }
            
            if (!plugin.getConditionManager().checkRegions(player, tag)) {
                player.sendMessage(plugin.getConfigManager().getMessages().getString("errors.region-not-allowed", "&cVocê não está na região permitida para usar esta tag."));
                return;
            }
            
            if (!plugin.getConditionManager().checkTimeRestriction(tag)) {
                player.sendMessage(plugin.getConfigManager().getMessages().getString("errors.time-restriction", "&cEsta tag não pode ser usada no momento."));
                return;
            }
        }
        
        long cooldown = plugin.getConfigManager().getConfig().getLong("tag-change-cooldown", 0);
        if (cooldown > 0) {
            Long lastChange = cooldowns.get(uuid);
            if (lastChange != null && System.currentTimeMillis() - lastChange < cooldown) {
                if (player != null) {
                    long remaining = cooldown - (System.currentTimeMillis() - lastChange);
                    player.sendMessage(plugin.getConfigManager().getMessages().getString("errors.cooldown", "&cAguarde " + (remaining / 1000) + " segundos antes de trocar de tag novamente."));
                }
                return;
            }
            cooldowns.put(uuid, System.currentTimeMillis());
        }
        
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags == null) return;
        
        for (PlayerTag playerTag : tags) {
            playerTag.setActive(playerTag.getTagId().equals(tagId));
        }
        
        activeTags.put(uuid, tagId);
        updatePlayerTagActive(uuid, tagId);
        
        if (player != null && tag != null) {
            TagChangeEvent event = new TagChangeEvent(player, tag, null);
            Bukkit.getPluginManager().callEvent(event);
            
            plugin.getDisplayNameManager().updatePlayerDisplayName(player);
            plugin.getTagService().updatePlayerTag(player);
            
            if (plugin.getIntegrationManager().isDiscordSRVEnabled()) {
                plugin.getIntegrationManager().getDiscordSRVIntegration().updatePlayerTag(player, tag);
            }
            
            if (plugin.getIntegrationManager().isBungeeCordEnabled()) {
                plugin.getIntegrationManager().getBungeeCordIntegration().syncTagToBungee(player, tag);
            }
            
            plugin.getLogManager().logTagEquip(uuid, tagId);
        }
    }
    
    public void removeActiveTag(UUID uuid) {
        activeTags.remove(uuid);
        
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags != null) {
            for (PlayerTag playerTag : tags) {
                playerTag.setActive(false);
            }
        }
        
        updatePlayerTagActive(uuid, null);
    }
    
    public String getActiveTag(UUID uuid) {
        return activeTags.get(uuid);
    }
    
    public List<PlayerTag> getPlayerTags(UUID uuid) {
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags == null) {
            loadPlayerTags(uuid);
            tags = playerTags.get(uuid);
        }
        return tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
    
    public List<PlayerTag> getFavoriteTags(UUID uuid) {
        List<PlayerTag> favorites = new ArrayList<>();
        List<PlayerTag> tags = getPlayerTags(uuid);
        
        for (PlayerTag playerTag : tags) {
            if (playerTag.isFavorite() && !playerTag.isExpired()) {
                favorites.add(playerTag);
            }
        }
        
        return favorites;
    }
    
    public void setFavorite(UUID uuid, String tagId, boolean favorite) {
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags == null) return;
        
        for (PlayerTag playerTag : tags) {
            if (playerTag.getTagId().equals(tagId)) {
                playerTag.setFavorite(favorite);
                updatePlayerTagFavorite(uuid, tagId, favorite);
                break;
            }
        }
    }
    
    private void savePlayerTag(PlayerTag playerTag) {
        plugin.getDatabaseManager().getAdapter().savePlayerTag(playerTag);
    }
    
    private void updatePlayerTagActive(UUID uuid, String tagId) {
        List<PlayerTag> tags = playerTags.get(uuid);
        if (tags != null) {
            for (PlayerTag playerTag : tags) {
                if (tagId != null && playerTag.getTagId().equals(tagId)) {
                    playerTag.setActive(true);
                    plugin.getDatabaseManager().getAdapter().updatePlayerTagActive(uuid, tagId, true);
                } else if (playerTag.isActive()) {
                    playerTag.setActive(false);
                    plugin.getDatabaseManager().getAdapter().updatePlayerTagActive(uuid, playerTag.getTagId(), false);
                }
            }
        }
    }
    
    private void updatePlayerTagFavorite(UUID uuid, String tagId, boolean favorite) {
        plugin.getDatabaseManager().getAdapter().updatePlayerTagFavorite(uuid, tagId, favorite);
    }
    
    private void deletePlayerTag(UUID uuid, String tagId) {
        plugin.getDatabaseManager().getAdapter().deletePlayerTag(uuid, tagId);
    }
    
    public void unloadPlayer(UUID uuid) {
        activeTags.remove(uuid);
        playerTags.remove(uuid);
        cooldowns.remove(uuid);
    }
}

