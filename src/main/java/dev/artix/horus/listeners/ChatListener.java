package dev.artix.horus.listeners;

import dev.artix.horus.Horus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    
    private final Horus plugin;
    
    public ChatListener(Horus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getChatSelectionManager().hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getChatSelectionManager().handleSelection(player, event.getMessage());
            });
        }
    }
}

