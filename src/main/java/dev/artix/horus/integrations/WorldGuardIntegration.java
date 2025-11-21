package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class WorldGuardIntegration {
    
    private final Horus plugin;
    private boolean enabled;
    
    public WorldGuardIntegration(Horus plugin) {
        this.plugin = plugin;
        this.enabled = checkWorldGuard();
    }
    
    private boolean checkWorldGuard() {
        return plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Set<String> getPlayerRegions(Player player) {
        Set<String> regions = new HashSet<>();
        
        if (!enabled) {
            return regions;
        }
        
        try {
            Class<?> worldGuardPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
            Object worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
            
            if (worldGuardPlugin == null) {
                return regions;
            }
            
            Object regionManager = worldGuardPluginClass.getMethod("getRegionManager", org.bukkit.World.class)
                    .invoke(worldGuardPlugin, player.getWorld());
            
            Object applicableRegions = regionManager.getClass().getMethod("getApplicableRegions", org.bukkit.Location.class)
                    .invoke(regionManager, player.getLocation());
            
            Object regionsSet = applicableRegions.getClass().getMethod("getRegions").invoke(applicableRegions);
            
            if (regionsSet instanceof java.util.Collection) {
                for (Object region : (java.util.Collection<?>) regionsSet) {
                    String regionName = region.getClass().getMethod("getId").invoke(region).toString();
                    regions.add(regionName);
                }
            }
        } catch (Exception e) {
        }
        
        return regions;
    }
}

