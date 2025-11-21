package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;

import java.util.Set;

public class ConditionManager {
    
    private final Horus plugin;
    
    public ConditionManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public boolean checkConditions(Player player, Tag tag) {
        if (tag.getConditions() == null || tag.getConditions().isEmpty()) {
            return true;
        }
        
        for (String condition : tag.getConditions()) {
            if (!checkCondition(player, condition)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean checkCondition(Player player, String condition) {
        switch (condition.toUpperCase()) {
            case "PVP":
            case "IN_PVP":
                return isInPvP(player);
            case "PVE":
            case "IN_PVE":
                return !isInPvP(player);
            case "IN_AIR":
                return !player.getLocation().getBlock().getType().isSolid();
            case "ON_GROUND":
                return player.getLocation().getBlock().getType().isSolid();
            case "IN_WATER":
                try {
                    return player.getLocation().getBlock().getType().name().contains("WATER");
                } catch (Exception e) {
                    return false;
                }
            case "SNEAKING":
                return player.isSneaking();
            case "SPRINTING":
                return player.isSprinting();
            case "FLYING":
                return player.isFlying();
            case "OP":
                return player.isOp();
            default:
                if (condition.startsWith("PERMISSION:")) {
                    String permission = condition.substring(11);
                    return player.hasPermission(permission);
                }
                if (condition.startsWith("WORLD:")) {
                    String worldName = condition.substring(6);
                    return player.getWorld().getName().equalsIgnoreCase(worldName);
                }
                return true;
        }
    }
    
    private boolean isInPvP(Player player) {
        return player.getWorld().getPVP();
    }
    
    public boolean checkRegions(Player player, Tag tag) {
        if (tag.getRegions() == null || tag.getRegions().isEmpty()) {
            return true;
        }
        
        if (!plugin.getIntegrationManager().isWorldGuardEnabled()) {
            return true;
        }
        
        Set<String> playerRegions = plugin.getIntegrationManager().getWorldGuardIntegration().getPlayerRegions(player);
        if (playerRegions.isEmpty()) {
            return false;
        }
        
        for (String requiredRegion : tag.getRegions()) {
            if (playerRegions.contains(requiredRegion)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean checkTimeRestriction(Tag tag) {
        if (tag.getTimeRestriction() == null || tag.getTimeRestriction().isEmpty()) {
            return true;
        }
        
        String[] parts = tag.getTimeRestriction().split("-");
        if (parts.length != 2) {
            return true;
        }
        
        try {
            int startHour = Integer.parseInt(parts[0].trim());
            int endHour = Integer.parseInt(parts[1].trim());
            
            int currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            
            if (startHour <= endHour) {
                return currentHour >= startHour && currentHour < endHour;
            } else {
                return currentHour >= startHour || currentHour < endHour;
            }
        } catch (NumberFormatException e) {
            return true;
        }
    }
}

