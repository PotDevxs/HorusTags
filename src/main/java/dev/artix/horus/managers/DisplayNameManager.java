package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class DisplayNameManager {
    
    private final Horus plugin;
    
    public DisplayNameManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void updatePlayerDisplayName(Player player) {
        String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
        if (activeTagId == null) {
            resetDisplayName(player);
            return;
        }
        
        Tag tag = plugin.getTagManager().getTag(activeTagId);
        if (tag == null) {
            resetDisplayName(player);
            return;
        }
        
        String formattedTag = plugin.getTagService().formatTag(player, tag);
        String displayName = formattedTag + player.getName();
        
        if (tag.getSuffix() != null && !tag.getSuffix().isEmpty()) {
            displayName += ColorUtil.translateColors(tag.getSuffix());
        }
        
        setDisplayName(player, displayName);
    }
    
    private void setDisplayName(Player player, String displayName) {
        try {
            player.setDisplayName(displayName);
            player.setPlayerListName(displayName);
        } catch (Exception e) {
        }
    }
    
    private void resetDisplayName(Player player) {
        try {
            player.setDisplayName(null);
            player.setPlayerListName(null);
        } catch (Exception e) {
        }
    }
    
    public void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerDisplayName(player);
        }
    }
}

