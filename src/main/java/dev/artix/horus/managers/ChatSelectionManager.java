package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatSelectionManager {
    
    private final Horus plugin;
    private final Map<UUID, ChatSelectionSession> activeSessions;
    
    public ChatSelectionManager(Horus plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }
    
    public void startTagSelection(Player player) {
        List<PlayerTag> playerTags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
        
        if (playerTags.isEmpty()) {
            player.sendMessage(ColorUtil.translateColors("&cVocê não possui nenhuma tag."));
            return;
        }
        
        List<Tag> availableTags = new ArrayList<>();
        for (PlayerTag playerTag : playerTags) {
            if (playerTag.isExpired()) continue;
            
            Tag tag = plugin.getTagManager().getTag(playerTag.getTagId());
            if (tag != null && tag.isEnabled()) {
                if (plugin.getPermissionManager().canUseTag(player, tag)) {
                    availableTags.add(tag);
                }
            }
        }
        
        if (availableTags.isEmpty()) {
            player.sendMessage(ColorUtil.translateColors("&cVocê não possui tags disponíveis."));
            return;
        }
        
        ChatSelectionSession session = new ChatSelectionSession(player.getUniqueId(), availableTags);
        activeSessions.put(player.getUniqueId(), session);
        
        displayTagSelection(player, session);
    }
    
    public void startCategorySelection(Player player) {
        Set<String> categories = new HashSet<>();
        for (Tag tag : plugin.getTagManager().getAllTags()) {
            if (tag.getCategory() != null && tag.isEnabled()) {
                categories.add(tag.getCategory());
            }
        }
        
        if (categories.isEmpty()) {
            player.sendMessage(ColorUtil.translateColors("&cNenhuma categoria disponível."));
            return;
        }
        
        List<String> categoryList = new ArrayList<>(categories);
        Collections.sort(categoryList);
        
        ChatSelectionSession session = new ChatSelectionSession(player.getUniqueId(), categoryList);
        activeSessions.put(player.getUniqueId(), session);
        
        displayCategorySelection(player, session);
    }
    
    public void startPurchaseSelection(Player player) {
        List<Tag> purchasableTags = plugin.getTagManager().getPurchasableTags();
        
        if (purchasableTags.isEmpty()) {
            player.sendMessage(ColorUtil.translateColors("&cNenhuma tag disponível para compra."));
            return;
        }
        
        ChatSelectionSession session = new ChatSelectionSession(player.getUniqueId(), purchasableTags, true);
        activeSessions.put(player.getUniqueId(), session);
        
        displayPurchaseSelection(player, session);
    }
    
    private void displayTagSelection(Player player, ChatSelectionSession session) {
        List<Tag> tags = (List<Tag>) session.getItems();
        String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
        
        dev.artix.horus.utils.ClickableMessageUtil.sendClickableTagList(
            player,
            tags,
            activeTagId,
            "/tag set"
        );
    }
    
    private void displayCategorySelection(Player player, ChatSelectionSession session) {
        player.sendMessage(ColorUtil.translateColors("&6&l=== Categorias de Tags ==="));
        player.sendMessage("");
        
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) session.getItems();
        
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            int number = i + 1;
            int tagCount = plugin.getTagManager().getTagsByCategory(category).size();
            
            player.sendMessage(ColorUtil.translateColors(
                String.format("&e%d. &f%s &7(&b%d tags&7)", number, category, tagCount)
            ));
        }
        
        player.sendMessage("");
        player.sendMessage(ColorUtil.translateColors("&7Digite o &enúmero &7da categoria, ou &c'cancelar' &7para cancelar."));
    }
    
    private void displayPurchaseSelection(Player player, ChatSelectionSession session) {
        player.sendMessage(ColorUtil.translateColors("&6&l=== Loja de Tags ==="));
        player.sendMessage("");
        
        @SuppressWarnings("unchecked")
        List<Tag> tags = (List<Tag>) session.getItems();
        boolean hasEconomy = plugin.getIntegrationManager().isVaultEnabled();
        
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            int number = i + 1;
            
            boolean hasTag = plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tag.getId());
            String status = hasTag ? "&a[POSSUI]" : "&e[COMPRAR]";
            
            double price = plugin.getEconomyManager().getPrice(tag);
            String priceText = hasEconomy ? String.format("&6$%.2f", price) : "&cEconomia não disponível";
            
            player.sendMessage(ColorUtil.translateColors(
                String.format("&e%d. %s &7- &f%s &7- %s", number, status, tag.getDisplayName(), priceText)
            ));
        }
        
        player.sendMessage("");
        player.sendMessage(ColorUtil.translateColors("&7Digite o &enúmero &7da tag que deseja comprar, ou &c'cancelar' &7para cancelar."));
    }
    
    public boolean handleSelection(Player player, String input) {
        ChatSelectionSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }
        
        if (input.equalsIgnoreCase("cancelar") || input.equalsIgnoreCase("cancel")) {
            cancelSelection(player);
            return true;
        }
        
        try {
            int number = Integer.parseInt(input);
            return processSelection(player, session, number);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.translateColors("&cPor favor, digite um número válido ou 'cancelar'."));
            return true;
        }
    }
    
    private boolean processSelection(Player player, ChatSelectionSession session, int number) {
        List<?> items = session.getItems();
        
        if (number < 1 || number > items.size()) {
            player.sendMessage(ColorUtil.translateColors("&cNúmero inválido. Por favor, escolha um número entre 1 e " + items.size() + "."));
            return true;
        }
        
        Object selected = items.get(number - 1);
        
        if (session.isPurchaseMode()) {
            if (selected instanceof Tag) {
                Tag tag = (Tag) selected;
                if (plugin.getEconomyManager().purchaseTag(player, tag)) {
                    player.sendMessage(ColorUtil.translateColors("&aTag comprada com sucesso!"));
                } else {
                    player.sendMessage(ColorUtil.translateColors("&cErro ao comprar tag. Verifique seu saldo."));
                }
            }
        } else if (selected instanceof Tag) {
            Tag tag = (Tag) selected;
            if (plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tag.getId())) {
                plugin.getPlayerTagManager().setActiveTag(player.getUniqueId(), tag.getId());
                player.sendMessage(ColorUtil.translateColors("&aTag equipada: &f" + tag.getDisplayName()));
            } else {
                player.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            }
        } else if (selected instanceof String) {
            String category = (String) selected;
            List<Tag> categoryTags = plugin.getTagManager().getTagsByCategory(category);
            List<Tag> availableTags = new ArrayList<>();
            
            for (Tag tag : categoryTags) {
                if (plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tag.getId()) && tag.isEnabled()) {
                    if (plugin.getPermissionManager().canUseTag(player, tag)) {
                        availableTags.add(tag);
                    }
                }
            }
            
            if (availableTags.isEmpty()) {
                player.sendMessage(ColorUtil.translateColors("&cVocê não possui tags nesta categoria."));
                cancelSelection(player);
                return true;
            }
            
            ChatSelectionSession newSession = new ChatSelectionSession(player.getUniqueId(), availableTags);
            activeSessions.put(player.getUniqueId(), newSession);
            displayTagSelection(player, newSession);
            return true;
        }
        
        cancelSelection(player);
        return true;
    }
    
    public void cancelSelection(Player player) {
        activeSessions.remove(player.getUniqueId());
        player.sendMessage(ColorUtil.translateColors("&7Seleção cancelada."));
    }
    
    public boolean hasActiveSession(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }
    
    public void removeSession(UUID uuid) {
        activeSessions.remove(uuid);
    }
    
    private static class ChatSelectionSession {
        private final UUID playerUUID;
        private final List<?> items;
        private final boolean purchaseMode;
        
        public ChatSelectionSession(UUID playerUUID, List<?> items) {
            this(playerUUID, items, false);
        }
        
        public ChatSelectionSession(UUID playerUUID, List<?> items, boolean purchaseMode) {
            this.playerUUID = playerUUID;
            this.items = items;
            this.purchaseMode = purchaseMode;
        }
        
        public UUID getPlayerUUID() {
            return playerUUID;
        }
        
        public List<?> getItems() {
            return items;
        }
        
        public boolean isPurchaseMode() {
            return purchaseMode;
        }
    }
}

