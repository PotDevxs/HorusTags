package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.gui.TagGUI;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    
    private final Horus plugin;
    private final Map<UUID, TagGUI> openGUIs;
    
    public GUIManager(Horus plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
    }
    
    public void openMainGUI(Player player) {
        closeGUI(player);
        TagGUI gui = new TagGUI(plugin, player);
        openGUIs.put(player.getUniqueId(), gui);
        gui.open();
    }
    
    public void openCategoryGUI(Player player, String category) {
        closeGUI(player);
        TagGUI gui = new TagGUI(plugin, player, category);
        openGUIs.put(player.getUniqueId(), gui);
        gui.open();
    }
    
    public void openPreviewGUI(Player player, String tagId) {
        closeGUI(player);
        TagGUI gui = new TagGUI(plugin, player);
        gui.openPreview(tagId);
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    public void closeGUI(Player player) {
        TagGUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.close();
        }
    }
    
    public void closeAllGUIs() {
        for (TagGUI gui : openGUIs.values()) {
            gui.close();
        }
        openGUIs.clear();
    }
    
    public boolean hasGUIOpen(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
}

