package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.database.DatabaseAdapter;
import dev.artix.horus.integrations.VaultIntegration;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class EconomyManager {
    
    private final Horus plugin;
    private VaultIntegration vaultIntegration;
    
    public EconomyManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void setVaultIntegration(VaultIntegration vaultIntegration) {
        this.vaultIntegration = vaultIntegration;
    }
    
    public boolean purchaseTag(Player player, Tag tag) {
        if (!tag.isPurchasable()) {
            return false;
        }
        
        if (plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tag.getId())) {
            return false;
        }
        
        double price = tag.getPrice();
        if (price <= 0) {
            return false;
        }
        
        if (vaultIntegration == null || !vaultIntegration.isEnabled()) {
            LoggerUtil.warning("Economia não disponível. Tag não pode ser comprada.");
            return false;
        }
        
        if (!vaultIntegration.has(player, price)) {
            return false;
        }
        
        if (!vaultIntegration.withdrawPlayer(player, price)) {
            return false;
        }
        
        recordPurchase(player.getUniqueId(), tag.getId(), price);
        plugin.getPlayerTagManager().giveTag(player.getUniqueId(), tag.getId(), "PURCHASE");
        
        return true;
    }
    
    public double getPrice(Tag tag) {
        double basePrice = tag.getPrice();
        
        if (plugin.getConfigManager().getConfig().getBoolean("economy.discounts.enabled", false)) {
            double discount = calculateDiscount(tag);
            basePrice = basePrice * (1 - discount);
        }
        
        return basePrice;
    }
    
    private double calculateDiscount(Tag tag) {
        double discount = 0.0;
        
        if (tag.isSeasonal() && plugin.getConfigManager().getConfig().getBoolean("economy.discounts.seasonal", false)) {
            discount += plugin.getConfigManager().getConfig().getDouble("economy.discounts.seasonal-amount", 0.1);
        }
        
        return Math.min(discount, 0.5);
    }
    
    private void recordPurchase(UUID uuid, String tagId, double price) {
        plugin.getDatabaseManager().getAdapter().savePurchase(uuid, tagId, price, System.currentTimeMillis());
    }
    
    public boolean canRefund(Player player, Tag tag) {
        if (!plugin.getConfigManager().getConfig().getBoolean("economy.refunds.enabled", false)) {
            return false;
        }
        
        long refundWindow = plugin.getConfigManager().getConfig().getLong("economy.refunds.window", 86400000);
        
        List<DatabaseAdapter.PurchaseRecord> purchases = plugin.getDatabaseManager().getAdapter().getPlayerPurchases(player.getUniqueId());
        for (DatabaseAdapter.PurchaseRecord purchase : purchases) {
            if (purchase.getTagId().equals(tag.getId())) {
                return System.currentTimeMillis() - purchase.getPurchasedAt() < refundWindow;
            }
        }
        
        return false;
    }
    
    public boolean refundTag(Player player, Tag tag) {
        if (!canRefund(player, tag)) {
            return false;
        }
        
        double price = tag.getPrice();
        if (vaultIntegration != null && vaultIntegration.isEnabled()) {
            vaultIntegration.depositPlayer(player, price);
        }
        
        plugin.getPlayerTagManager().removeTag(player.getUniqueId(), tag.getId());
        return true;
    }
}

