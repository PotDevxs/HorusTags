package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GiftManager {
    
    private final Horus plugin;
    
    public GiftManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public boolean sendGift(Player sender, Player target, String tagId) {
        if (!plugin.getPlayerTagManager().hasTag(sender.getUniqueId(), tagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            return false;
        }
        
        Tag tag = plugin.getTagManager().getTag(tagId);
        if (tag == null || !tag.isGiftable()) {
            sender.sendMessage(ColorUtil.translateColors("&cEsta tag não pode ser presenteada."));
            return false;
        }
        
        if (plugin.getPlayerTagManager().hasTag(target.getUniqueId(), tagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cO jogador já possui esta tag."));
            return false;
        }
        
        if (plugin.getPermissionManager().hasTagLimit(target) && 
            !plugin.getPermissionManager().canUseTag(target, tag)) {
            sender.sendMessage(ColorUtil.translateColors("&cO jogador não pode receber mais tags ou não tem permissão para esta tag."));
            return false;
        }
        
        plugin.getPlayerTagManager().removeTag(sender.getUniqueId(), tagId);
        plugin.getPlayerTagManager().giveTag(target.getUniqueId(), tagId, "GIFT");
        
        sender.sendMessage(ColorUtil.translateColors("&aTag presenteada com sucesso para &f" + target.getName() + "&a!"));
        
        if (target.isOnline()) {
            target.sendMessage(ColorUtil.translateColors("&6&lVocê recebeu um presente!"));
            target.sendMessage(ColorUtil.translateColors("&7" + sender.getName() + " &7te presenteou com a tag: &f" + tag.getDisplayName()));
            plugin.getNotificationManager().sendTagObtainedNotification(target, tag);
        }
        
        plugin.getLogManager().logGift(sender.getUniqueId(), target.getUniqueId(), tagId);
        
        return true;
    }
    
    public boolean sendGiftOffline(Player sender, UUID targetUUID, String tagId) {
        if (!plugin.getPlayerTagManager().hasTag(sender.getUniqueId(), tagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            return false;
        }
        
        Tag tag = plugin.getTagManager().getTag(tagId);
        if (tag == null || !tag.isGiftable()) {
            sender.sendMessage(ColorUtil.translateColors("&cEsta tag não pode ser presenteada."));
            return false;
        }
        
        plugin.getPlayerTagManager().removeTag(sender.getUniqueId(), tagId);
        plugin.getPlayerTagManager().giveTag(targetUUID, tagId, "GIFT");
        
        String targetName = Bukkit.getOfflinePlayer(targetUUID).getName();
        sender.sendMessage(ColorUtil.translateColors("&aTag presenteada com sucesso para &f" + targetName + "&a!"));
        
        plugin.getLogManager().logGift(sender.getUniqueId(), targetUUID, tagId);
        
        return true;
    }
}

