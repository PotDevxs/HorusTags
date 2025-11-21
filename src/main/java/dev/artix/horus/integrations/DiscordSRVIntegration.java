package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;

public class DiscordSRVIntegration {
    
    private final Horus plugin;
    private boolean enabled;
    
    public DiscordSRVIntegration(Horus plugin) {
        this.plugin = plugin;
        this.enabled = checkDiscordSRV();
    }
    
    private boolean checkDiscordSRV() {
        return plugin.getServer().getPluginManager().getPlugin("DiscordSRV") != null;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void updatePlayerTag(Player player, Tag tag) {
        if (!enabled) {
            return;
        }
        
        try {
            Class<?> discordSRV = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Object pluginInstance = discordSRV.getMethod("getPlugin").invoke(null);
            
            if (pluginInstance != null) {
                Object accountManager = pluginInstance.getClass().getMethod("getAccountLinkManager").invoke(pluginInstance);
                
                if (accountManager != null) {
                    Object discordId = accountManager.getClass().getMethod("getDiscordId", java.util.UUID.class)
                            .invoke(accountManager, player.getUniqueId());
                    
                    if (discordId != null) {
                        String tagDisplay = tag != null ? tag.getDisplayName() : "";
                        updateDiscordNickname(discordId.toString(), player.getName(), tagDisplay);
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    
    private void updateDiscordNickname(String discordId, String playerName, String tag) {
        try {
            Class<?> discordSRV = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Object pluginInstance = discordSRV.getMethod("getPlugin").invoke(null);
            
            if (pluginInstance != null) {
                Object jda = pluginInstance.getClass().getMethod("getJda").invoke(pluginInstance);
                
                if (jda != null) {
                    Object guild = jda.getClass().getMethod("getGuildById", long.class)
                            .invoke(jda, getMainGuildId());
                    
                    if (guild != null) {
                        Object member = guild.getClass().getMethod("getMemberById", long.class)
                                .invoke(guild, Long.parseLong(discordId));
                        
                        if (member != null) {
                            String nickname = tag.isEmpty() ? playerName : tag + " " + playerName;
                            member.getClass().getMethod("modifyNickname", String.class).invoke(member, nickname);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    
    private long getMainGuildId() {
        try {
            Class<?> discordSRV = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Object pluginInstance = discordSRV.getMethod("getPlugin").invoke(null);
            
            if (pluginInstance != null) {
                Object config = pluginInstance.getClass().getMethod("getConfig").invoke(pluginInstance);
                Object guildId = config.getClass().getMethod("getLong", String.class)
                        .invoke(config, "DiscordGuildId");
                if (guildId instanceof Long) {
                    return ((Long) guildId).longValue();
                } else if (guildId instanceof Number) {
                    return ((Number) guildId).longValue();
                }
            }
        } catch (Exception e) {
        }
        return 0L;
    }
}

