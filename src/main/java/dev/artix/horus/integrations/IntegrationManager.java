package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.Bukkit;

public class IntegrationManager {
    
    private final Horus plugin;
    private VaultIntegration vaultIntegration;
    private LuckPermsIntegration luckPermsIntegration;
    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private WorldGuardIntegration worldGuardIntegration;
    private DiscordSRVIntegration discordSRVIntegration;
    private BungeeCordIntegration bungeeCordIntegration;
    
    public IntegrationManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void loadIntegrations() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultIntegration = new VaultIntegration(plugin);
            if (vaultIntegration.isEnabled()) {
                plugin.getEconomyManager().setVaultIntegration(vaultIntegration);
                LoggerUtil.info("Vault integrado com sucesso!");
            }
        } else {
            LoggerUtil.warning("Vault não encontrado. Sistema de economia desabilitado.");
        }
        
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            luckPermsIntegration = new LuckPermsIntegration(plugin);
            if (luckPermsIntegration.isEnabled()) {
                plugin.getPermissionManager().setLuckPermsIntegration(luckPermsIntegration);
                LoggerUtil.info("LuckPerms integrado com sucesso!");
            }
        } else {
            LoggerUtil.warning("LuckPerms não encontrado. Algumas funcionalidades de permissão podem estar limitadas.");
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIIntegration = new PlaceholderAPIIntegration(plugin);
            placeholderAPIIntegration.registerExpansion();
            LoggerUtil.info("PlaceholderAPI integrado com sucesso!");
        } else {
            LoggerUtil.warning("PlaceholderAPI não encontrado. Placeholders desabilitados.");
        }
        
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardIntegration = new WorldGuardIntegration(plugin);
            if (worldGuardIntegration.isEnabled()) {
                LoggerUtil.info("WorldGuard integrado com sucesso!");
            }
        } else {
            LoggerUtil.warning("WorldGuard não encontrado. Verificação de regiões desabilitada.");
        }
        
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            discordSRVIntegration = new DiscordSRVIntegration(plugin);
            if (discordSRVIntegration.isEnabled()) {
                LoggerUtil.info("DiscordSRV integrado com sucesso!");
            }
        } else {
            LoggerUtil.warning("DiscordSRV não encontrado. Sincronização com Discord desabilitada.");
        }
        
        if (Bukkit.getPluginManager().getPlugin("BungeeCord") != null) {
            bungeeCordIntegration = new BungeeCordIntegration(plugin);
            if (bungeeCordIntegration.isEnabled()) {
                plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
                LoggerUtil.info("BungeeCord integrado com sucesso!");
            }
        } else {
            LoggerUtil.warning("BungeeCord não encontrado. Sincronização entre servidores desabilitada.");
        }
    }
    
    public boolean isVaultEnabled() {
        return vaultIntegration != null && vaultIntegration.isEnabled();
    }
    
    public boolean isLuckPermsEnabled() {
        return luckPermsIntegration != null && luckPermsIntegration.isEnabled();
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIIntegration != null && placeholderAPIIntegration.isExpansionRegistered();
    }
    
    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
    
    public LuckPermsIntegration getLuckPermsIntegration() {
        return luckPermsIntegration;
    }
    
    public PlaceholderAPIIntegration getPlaceholderAPI() {
        return placeholderAPIIntegration;
    }
    
    public boolean isWorldGuardEnabled() {
        return worldGuardIntegration != null && worldGuardIntegration.isEnabled();
    }
    
    public boolean isDiscordSRVEnabled() {
        return discordSRVIntegration != null && discordSRVIntegration.isEnabled();
    }
    
    public boolean isBungeeCordEnabled() {
        return bungeeCordIntegration != null && bungeeCordIntegration.isEnabled();
    }
    
    public WorldGuardIntegration getWorldGuardIntegration() {
        return worldGuardIntegration;
    }
    
    public DiscordSRVIntegration getDiscordSRVIntegration() {
        return discordSRVIntegration;
    }
    
    public BungeeCordIntegration getBungeeCordIntegration() {
        return bungeeCordIntegration;
    }
}

