package dev.artix.horus.gui;

import dev.artix.horus.Horus;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TagGUI implements Listener {
    
    private final Horus plugin;
    private final Player player;
    private Inventory inventory;
    private String currentCategory;
    private int currentPage;
    
    public TagGUI(Horus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.currentPage = 1;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public TagGUI(Horus plugin, Player player, String category) {
        this(plugin, player);
        this.currentCategory = category;
    }
    
    public void open() {
        int size = 54;
        String title = plugin.getConfigManager().getMessages().getString("gui.title", "&6Tags");
        inventory = Bukkit.createInventory(null, size, ColorUtil.translateColors(title));
        
        if (currentCategory != null) {
            openCategory();
        } else {
            openMain();
        }
        
        player.openInventory(inventory);
    }
    
    private void openMain() {
        inventory.clear();
        
        List<String> categories = new ArrayList<>();
        for (Tag tag : plugin.getTagManager().getAllTags()) {
            if (tag.getCategory() != null && !categories.contains(tag.getCategory())) {
                categories.add(tag.getCategory());
            }
        }
        
        int slot = 10;
        for (String category : categories) {
            if (slot >= 44) break;
            
            ItemStack item = createCategoryItem(category);
            inventory.setItem(slot, item);
            
            slot++;
            if ((slot - 9) % 9 == 0) {
                slot += 2;
            }
        }
        
        ItemStack favoritesItem = createFavoritesItem();
        inventory.setItem(49, favoritesItem);
    }
    
    private void openCategory() {
        inventory.clear();
        
        List<Tag> tags = plugin.getTagManager().getTagsByCategory(currentCategory);
        List<PlayerTag> playerTags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
        
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) tags.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;
        
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, tags.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Tag tag = tags.get(i);
            boolean hasTag = playerTags.stream().anyMatch(pt -> pt.getTagId().equals(tag.getId()) && !pt.isExpired());
            boolean isFavorite = playerTags.stream().anyMatch(pt -> pt.getTagId().equals(tag.getId()) && pt.isFavorite());
            ItemStack item = createTagItem(tag, hasTag, isFavorite);
            inventory.setItem(slot, item);
            slot++;
        }
        
        ItemStack backItem = createBackItem();
        inventory.setItem(45, backItem);
        
        if (currentPage > 1) {
            ItemStack prevPage = createPageItem(false);
            inventory.setItem(46, prevPage);
        }
        
        if (currentPage < totalPages) {
            ItemStack nextPage = createPageItem(true);
            inventory.setItem(52, nextPage);
        }
        
        if (totalPages > 1) {
            ItemStack pageInfo = createPageInfoItem(currentPage, totalPages);
            inventory.setItem(49, pageInfo);
        }
    }
    
    private void openFavorites() {
        inventory.clear();
        currentCategory = "FAVORITES";
        
        List<PlayerTag> playerTags = plugin.getPlayerTagManager().getFavoriteTags(player.getUniqueId());
        List<Tag> favoriteTags = new ArrayList<>();
        for (PlayerTag playerTag : playerTags) {
            Tag tag = plugin.getTagManager().getTag(playerTag.getTagId());
            if (tag != null && !playerTag.isExpired()) {
                favoriteTags.add(tag);
            }
        }
        
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) favoriteTags.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;
        
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, favoriteTags.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Tag tag = favoriteTags.get(i);
            ItemStack item = createTagItem(tag, true, true);
            inventory.setItem(slot, item);
            slot++;
        }
        
        ItemStack backItem = createBackItem();
        inventory.setItem(45, backItem);
        
        if (currentPage > 1) {
            ItemStack prevPage = createPageItem(false);
            inventory.setItem(46, prevPage);
        }
        
        if (currentPage < totalPages) {
            ItemStack nextPage = createPageItem(true);
            inventory.setItem(52, nextPage);
        }
        
        if (totalPages > 1) {
            ItemStack pageInfo = createPageInfoItem(currentPage, totalPages);
            inventory.setItem(49, pageInfo);
        }
    }
    
    public void openPreview(String tagId) {
        Tag tag = plugin.getTagManager().getTag(tagId);
        if (tag == null) return;
        
        int size = 27;
        String title = plugin.getConfigManager().getMessages().getString("gui.preview-title", "&6Preview: {tag}")
                .replace("{tag}", tag.getDisplayName());
        inventory = Bukkit.createInventory(null, size, ColorUtil.translateColors(title));
        
        ItemStack previewItem = createPreviewItem(tag);
        inventory.setItem(13, previewItem);
        
        boolean hasTag = plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tagId);
        if (hasTag) {
            ItemStack equipItem = createEquipItem(tag, tagId);
            inventory.setItem(11, equipItem);
        } else if (tag.isPurchasable()) {
            ItemStack purchaseItem = createPurchaseItem(tag, tagId);
            inventory.setItem(15, purchaseItem);
        }
        
        ItemStack backItem = createBackItem();
        inventory.setItem(18, backItem);
        
        player.openInventory(inventory);
    }
    
    private ItemStack createCategoryItem(String category) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors("&6" + category));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createTagItem(Tag tag, boolean hasTag) {
        return createTagItem(tag, hasTag, false);
    }
    
    private ItemStack createTagItem(Tag tag, boolean hasTag, boolean isFavorite) {
        Material material = hasTag ? Material.NAME_TAG : Material.PAPER;
        if (isFavorite) {
            material = Material.NETHER_STAR;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = ColorUtil.translateColors(tag.getDisplayName());
        if (isFavorite) {
            displayName = ColorUtil.translateColors("&6★ &r" + displayName);
        }
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        if (tag.getDescription() != null) {
            for (String line : tag.getDescription()) {
                lore.add(ColorUtil.translateColors("&7" + line));
            }
        }
        lore.add("");
        lore.add(ColorUtil.translateColors(hasTag ? "&aVocê possui esta tag" : "&cVocê não possui esta tag"));
        
        if (hasTag) {
            String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
            if (tag.getId().equals(activeTagId)) {
                lore.add(ColorUtil.translateColors("&a[ATIVA]"));
            }
        }
        
        if (tag.isPurchasable() && !hasTag) {
            double price = plugin.getEconomyManager().getPrice(tag);
            lore.add(ColorUtil.translateColors("&ePreço: &6$" + price));
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPreviewItem(Tag tag) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors("&6Preview: &f" + tag.getDisplayName()));
        
        List<String> lore = new ArrayList<>();
        if (tag.getPrefix() != null) {
            lore.add(ColorUtil.translateColors("&7Prefixo: " + tag.getPrefix()));
        }
        if (tag.getSuffix() != null) {
            lore.add(ColorUtil.translateColors("&7Sufixo: " + tag.getSuffix()));
        }
        if (tag.getRarity() != null) {
            lore.add(ColorUtil.translateColors("&7Raridade: &f" + tag.getRarity().name()));
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createEquipItem(Tag tag, String tagId) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors("&aEquipar Tag"));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.translateColors("&7Tag ID: &f" + tagId));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPurchaseItem(Tag tag, String tagId) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        double price = plugin.getEconomyManager().getPrice(tag);
        meta.setDisplayName(ColorUtil.translateColors("&eComprar por &6$" + price));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.translateColors("&7Tag ID: &f" + tagId));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createFavoritesItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors("&6Tags Favoritas"));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors("&cVoltar"));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPageItem(boolean next) {
        ItemStack item = new ItemStack(next ? Material.ARROW : Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors(next ? "&aPróxima Página" : "&cPágina Anterior"));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPageInfoItem(int current, int total) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors("&7Página " + current + "/" + total));
        item.setItemMeta(meta);
        return item;
    }
    
    private String extractTagIdFromLore(ItemMeta meta) {
        if (meta == null || !meta.hasLore()) {
            return null;
        }
        
        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("Tag ID:")) {
                String tagId = line.replaceAll("§[0-9a-fk-or]", "").replace("Tag ID:", "").trim();
                return tagId;
            }
        }
        return null;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = meta.getDisplayName();
        
        if (displayName.contains("Voltar")) {
            if (currentCategory != null && !currentCategory.equals("FAVORITES")) {
                currentCategory = null;
                currentPage = 1;
                open();
            } else if (currentCategory != null && currentCategory.equals("FAVORITES")) {
                currentCategory = null;
                currentPage = 1;
                open();
            } else {
                close();
            }
        } else if (displayName.contains("Próxima Página")) {
            currentPage++;
            if (currentCategory != null && currentCategory.equals("FAVORITES")) {
                openFavorites();
            } else if (currentCategory != null) {
                openCategory();
            }
        } else if (displayName.contains("Página Anterior")) {
            currentPage--;
            if (currentCategory != null && currentCategory.equals("FAVORITES")) {
                openFavorites();
            } else if (currentCategory != null) {
                openCategory();
            }
        } else if (displayName.contains("Tags Favoritas")) {
            currentCategory = "FAVORITES";
            currentPage = 1;
            openFavorites();
        } else if (displayName.contains("Equipar")) {
            String tagId = extractTagIdFromLore(meta);
            if (tagId != null && plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tagId)) {
                Tag tag = plugin.getTagManager().getTag(tagId);
                if (tag != null) {
                    plugin.getPlayerTagManager().setActiveTag(player.getUniqueId(), tagId);
                    player.sendMessage(ColorUtil.translateColors("&aTag equipada: &f" + tag.getDisplayName()));
                    close();
                }
            }
        } else if (displayName.contains("Comprar")) {
            String tagId = extractTagIdFromLore(meta);
            if (tagId != null) {
                Tag tag = plugin.getTagManager().getTag(tagId);
                if (tag != null) {
                    if (plugin.getEconomyManager().purchaseTag(player, tag)) {
                        player.sendMessage(ColorUtil.translateColors("&aTag comprada com sucesso!"));
                        open();
                    } else {
                        player.sendMessage(ColorUtil.translateColors("&cErro ao comprar tag. Verifique seu saldo."));
                    }
                }
            }
        } else {
            for (Tag tag : plugin.getTagManager().getAllTags()) {
                String tagDisplayName = tag.getDisplayName();
                if (displayName.contains(tagDisplayName) || (displayName.contains("★") && displayName.contains(tagDisplayName))) {
                    openPreview(tag.getId());
                    return;
                }
            }
            
            List<String> categories = new ArrayList<>();
            for (Tag tag : plugin.getTagManager().getAllTags()) {
                if (tag.getCategory() != null && !categories.contains(tag.getCategory())) {
                    categories.add(tag.getCategory());
                }
            }
            for (String category : categories) {
                if (displayName.contains(category)) {
                    currentCategory = category;
                    currentPage = 1;
                    open();
                    return;
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            close();
        }
    }
    
    public void close() {
        player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}

