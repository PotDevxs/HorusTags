package dev.artix.horus.listeners;

import dev.artix.horus.Horus;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PlayerListener implements Listener {
    
    private final Horus plugin;
    
    public PlayerListener(Horus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getPlayerTagManager().loadPlayerTags(player.getUniqueId());
                plugin.getCacheManager().cachePlayer(player.getUniqueId());
                
                String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
                if (activeTagId != null) {
                    Tag tag = plugin.getTagManager().getTag(activeTagId);
                    if (tag != null) {
                        plugin.getTagService().updatePlayerTag(player);
                        plugin.getDisplayNameManager().updatePlayerDisplayName(player);
                        
                        if (plugin.getTitleManager().getTitleConfig(tag.getId()) != null) {
                            plugin.getTitleManager().startTitleAnimation(player, tag);
                        }
                        
                        if (plugin.getIntegrationManager().isDiscordSRVEnabled()) {
                            plugin.getIntegrationManager().getDiscordSRVIntegration().updatePlayerTag(player, tag);
                        }
                        
                        if (plugin.getIntegrationManager().isBungeeCordEnabled()) {
                            plugin.getIntegrationManager().getBungeeCordIntegration().syncTagToBungee(player, tag);
                        }
                    }
                }
                
                plugin.getCollectionManager().checkAndRewardCollection(player.getUniqueId(), null);
                checkExpiringTags(player);
                
                if (plugin.getConfigManager().getConfig().getBoolean("daily-tags.notify", true)) {
                    List<String> categories = plugin.getConfigManager().getConfig().getStringList("daily-tags.categories.default");
                    boolean hasAvailableDailyTags = false;
                    for (String category : categories) {
                        List<dev.artix.horus.models.Tag> dailyTags = plugin.getRandomTagManager().getDailyTags(category);
                        for (dev.artix.horus.models.Tag dailyTag : dailyTags) {
                            if (!plugin.getPlayerTagManager().hasTag(player.getUniqueId(), dailyTag.getId())) {
                                hasAvailableDailyTags = true;
                                break;
                            }
                        }
                        if (hasAvailableDailyTags) break;
                    }
                    
                    if (hasAvailableDailyTags) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            player.sendMessage(dev.artix.horus.utils.ColorUtil.translateColors("&6&lTags Diárias Disponíveis!"));
                            player.sendMessage(dev.artix.horus.utils.ColorUtil.translateColors("&7Use &e/tag daily &7para receber uma tag aleatória!"));
                        }, 60L);
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerTagManager().unloadPlayer(player.getUniqueId());
        plugin.getCacheManager().clearPlayerCache(player.getUniqueId());
        plugin.getGuiManager().closeGUI(player);
        plugin.getChatSelectionManager().removeSession(player.getUniqueId());
        plugin.getTitleManager().stopTitleAnimation(player);
    }
    
    private void checkExpiringTags(Player player) {
        List<PlayerTag> tags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
        
        for (PlayerTag playerTag : tags) {
            if (playerTag.isExpired()) {
                continue;
            }
            
            long timeRemaining = playerTag.getTimeRemaining();
            if (timeRemaining > 0 && timeRemaining < 86400000) {
                Tag tag = plugin.getTagManager().getTag(playerTag.getTagId());
                if (tag != null) {
                    plugin.getNotificationManager().sendTagExpiringNotification(player, tag, timeRemaining);
                }
            }
        }
    }
}

