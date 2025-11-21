package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {
    
    private final Horus plugin;
    private final Map<UUID, TradeRequest> pendingTrades;
    
    public TradeManager(Horus plugin) {
        this.plugin = plugin;
        this.pendingTrades = new HashMap<>();
    }
    
    public boolean initiateTrade(Player sender, Player target, String senderTagId, String targetTagId) {
        if (!plugin.getPlayerTagManager().hasTag(sender.getUniqueId(), senderTagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            return false;
        }
        
        if (targetTagId != null && !plugin.getPlayerTagManager().hasTag(target.getUniqueId(), targetTagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cO jogador não possui esta tag."));
            return false;
        }
        
        Tag senderTag = plugin.getTagManager().getTag(senderTagId);
        if (senderTag == null || !senderTag.isTradeable()) {
            sender.sendMessage(ColorUtil.translateColors("&cEsta tag não pode ser trocada."));
            return false;
        }
        
        Tag targetTag = null;
        if (targetTagId != null) {
            targetTag = plugin.getTagManager().getTag(targetTagId);
            if (targetTag == null || !targetTag.isTradeable()) {
                sender.sendMessage(ColorUtil.translateColors("&cA tag do jogador não pode ser trocada."));
                return false;
            }
        }
        
        TradeRequest request = new TradeRequest(sender.getUniqueId(), target.getUniqueId(), senderTagId, targetTagId);
        pendingTrades.put(target.getUniqueId(), request);
        
        String message = ColorUtil.translateColors("&6" + sender.getName() + " &7quer trocar a tag &f" + senderTag.getDisplayName());
        if (targetTag != null) {
            message += ColorUtil.translateColors(" &7pela tag &f" + targetTag.getDisplayName());
        } else {
            message += ColorUtil.translateColors(" &7(por nada)");
        }
        target.sendMessage(message);
        target.sendMessage(ColorUtil.translateColors("&7Use &a/tag trade accept &7para aceitar ou &c/tag trade deny &7para negar."));
        
        sender.sendMessage(ColorUtil.translateColors("&aSolicitação de troca enviada para &f" + target.getName()));
        
        return true;
    }
    
    public boolean acceptTrade(Player player) {
        TradeRequest request = pendingTrades.get(player.getUniqueId());
        if (request == null) {
            player.sendMessage(ColorUtil.translateColors("&cVocê não tem nenhuma solicitação de troca pendente."));
            return false;
        }
        
        Player sender = Bukkit.getPlayer(request.getSenderUUID());
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(ColorUtil.translateColors("&cO jogador não está mais online."));
            pendingTrades.remove(player.getUniqueId());
            return false;
        }
        
        if (!plugin.getPlayerTagManager().hasTag(sender.getUniqueId(), request.getSenderTagId())) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui mais esta tag."));
            player.sendMessage(ColorUtil.translateColors("&cA troca foi cancelada."));
            pendingTrades.remove(player.getUniqueId());
            return false;
        }
        
        if (request.getTargetTagId() != null) {
            if (!plugin.getPlayerTagManager().hasTag(player.getUniqueId(), request.getTargetTagId())) {
                player.sendMessage(ColorUtil.translateColors("&cVocê não possui mais esta tag."));
                sender.sendMessage(ColorUtil.translateColors("&cA troca foi cancelada."));
                pendingTrades.remove(player.getUniqueId());
                return false;
            }
            
            plugin.getPlayerTagManager().removeTag(sender.getUniqueId(), request.getSenderTagId());
            plugin.getPlayerTagManager().removeTag(player.getUniqueId(), request.getTargetTagId());
            
            plugin.getPlayerTagManager().giveTag(sender.getUniqueId(), request.getTargetTagId(), "TRADE");
            plugin.getPlayerTagManager().giveTag(player.getUniqueId(), request.getSenderTagId(), "TRADE");
            
            sender.sendMessage(ColorUtil.translateColors("&aTroca realizada com sucesso!"));
            player.sendMessage(ColorUtil.translateColors("&aTroca realizada com sucesso!"));
        } else {
            plugin.getPlayerTagManager().removeTag(sender.getUniqueId(), request.getSenderTagId());
            plugin.getPlayerTagManager().giveTag(player.getUniqueId(), request.getSenderTagId(), "TRADE");
            
            sender.sendMessage(ColorUtil.translateColors("&aTag enviada com sucesso!"));
            player.sendMessage(ColorUtil.translateColors("&aTag recebida com sucesso!"));
        }
        
        pendingTrades.remove(player.getUniqueId());
        plugin.getLogManager().logTrade(sender.getUniqueId(), player.getUniqueId(), request.getSenderTagId(), request.getTargetTagId());
        
        return true;
    }
    
    public boolean denyTrade(Player player) {
        TradeRequest request = pendingTrades.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage(ColorUtil.translateColors("&cVocê não tem nenhuma solicitação de troca pendente."));
            return false;
        }
        
        Player sender = Bukkit.getPlayer(request.getSenderUUID());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(ColorUtil.translateColors("&c" + player.getName() + " negou sua solicitação de troca."));
        }
        
        player.sendMessage(ColorUtil.translateColors("&cSolicitação de troca negada."));
        return true;
    }
    
    public void removeTradeRequest(UUID uuid) {
        pendingTrades.remove(uuid);
    }
    
    private static class TradeRequest {
        private final UUID senderUUID;
        private final UUID targetUUID;
        private final String senderTagId;
        private final String targetTagId;
        
        public TradeRequest(UUID senderUUID, UUID targetUUID, String senderTagId, String targetTagId) {
            this.senderUUID = senderUUID;
            this.targetUUID = targetUUID;
            this.senderTagId = senderTagId;
            this.targetTagId = targetTagId;
        }
        
        public UUID getSenderUUID() {
            return senderUUID;
        }
        
        public UUID getTargetUUID() {
            return targetUUID;
        }
        
        public String getSenderTagId() {
            return senderTagId;
        }
        
        public String getTargetTagId() {
            return targetTagId;
        }
    }
}

