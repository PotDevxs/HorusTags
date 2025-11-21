package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class LuckPermsIntegration {
    
    private final Horus plugin;
    private LuckPerms luckPerms;
    private boolean enabled;
    
    public LuckPermsIntegration(Horus plugin) {
        this.plugin = plugin;
        this.enabled = setupLuckPerms();
    }
    
    private boolean setupLuckPerms() {
        try {
            luckPerms = LuckPermsProvider.get();
            return luckPerms != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Set<String> getPlayerGroups(Player player) {
        Set<String> groups = new HashSet<>();
        
        if (!enabled) {
            return groups;
        }
        
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                user.getInheritedGroups(user.getQueryOptions()).forEach(group -> {
                    groups.add(group.getName());
                });
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao obter grupos do jogador: " + e.getMessage());
        }
        
        return groups;
    }
    
    public String getPrimaryGroup(Player player) {
        if (!enabled) {
            return "default";
        }
        
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getPrimaryGroup();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao obter grupo primário: " + e.getMessage());
        }
        
        return "default";
    }
}

