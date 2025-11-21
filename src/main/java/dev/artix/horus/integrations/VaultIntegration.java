package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {
    
    private final Horus plugin;
    private Economy economy;
    private boolean enabled;
    
    public VaultIntegration(Horus plugin) {
        this.plugin = plugin;
        this.enabled = setupEconomy();
    }
    
    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean has(Player player, double amount) {
        if (!enabled) return false;
        return economy.has(player, amount);
    }
    
    public boolean withdrawPlayer(Player player, double amount) {
        if (!enabled) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    public boolean depositPlayer(Player player, double amount) {
        if (!enabled) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    public double getBalance(Player player) {
        if (!enabled) return 0.0;
        return economy.getBalance(player);
    }
}

