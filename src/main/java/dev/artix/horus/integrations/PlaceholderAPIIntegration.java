package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class PlaceholderAPIIntegration extends PlaceholderExpansion {
    
    private final Horus plugin;
    private boolean registered;
    
    public PlaceholderAPIIntegration(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void registerExpansion() {
        if (super.register()) {
            registered = true;
        }
    }
    
    @Override
    public String getIdentifier() {
        return "horus";
    }
    
    @Override
    public String getAuthor() {
        return "Artix";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        
        String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
        Tag tag = activeTagId != null ? plugin.getTagManager().getTag(activeTagId) : null;
        
        switch (identifier.toLowerCase()) {
            case "tag":
            case "active_tag":
                if (tag != null) {
                    return tag.getDisplayName();
                }
                return "";
                
            case "tag_prefix":
                if (tag != null && tag.getPrefix() != null) {
                    return plugin.getTagService().formatTag(player, tag);
                }
                return "";
                
            case "tag_suffix":
                if (tag != null && tag.getSuffix() != null) {
                    return tag.getSuffix();
                }
                return "";
                
            case "tag_id":
                return activeTagId != null ? activeTagId : "";
                
            case "tag_name":
                if (tag != null) {
                    return tag.getName();
                }
                return "";
                
            case "tag_rarity":
                if (tag != null && tag.getRarity() != null) {
                    return tag.getRarity().name();
                }
                return "";
                
            case "tag_category":
                if (tag != null && tag.getCategory() != null) {
                    return tag.getCategory();
                }
                return "";
                
            case "tag_price":
                if (tag != null) {
                    return String.valueOf(plugin.getEconomyManager().getPrice(tag));
                }
                return "0";
                
            case "tag_count":
                return String.valueOf(plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId()).size());
                
            case "favorite_count":
                return String.valueOf(plugin.getPlayerTagManager().getFavoriteTags(player.getUniqueId()).size());
                
            case "tag_type":
                if (tag != null && tag.getType() != null) {
                    return tag.getType().name();
                }
                return "";
                
            case "tag_priority":
                if (tag != null) {
                    return String.valueOf(tag.getPriority());
                }
                return "0";
                
            default:
                return null;
        }
    }
    
    public boolean isExpansionRegistered() {
        return registered;
    }
    
    public String replacePlaceholders(Player player, String text) {
        if (player == null || text == null) {
            return text;
        }
        
        try {
            Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method setPlaceholdersMethod = placeholderAPI.getMethod("setPlaceholders", Player.class, String.class);
            return (String) setPlaceholdersMethod.invoke(null, player, text);
        } catch (Exception e) {
            return text;
        }
    }
}

