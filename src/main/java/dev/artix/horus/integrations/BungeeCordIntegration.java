package dev.artix.horus.integrations;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BungeeCordIntegration {
    
    private final Horus plugin;
    private boolean enabled;
    
    public BungeeCordIntegration(Horus plugin) {
        this.plugin = plugin;
        this.enabled = checkBungeeCord();
    }
    
    private boolean checkBungeeCord() {
        return plugin.getServer().getPluginManager().getPlugin("BungeeCord") != null;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void syncTagToBungee(Player player, Tag tag) {
        if (!enabled) {
            return;
        }
        
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            
            out.writeUTF("HorusTag");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(tag != null ? tag.getId() : "");
            out.writeUTF(tag != null ? tag.getPrefix() : "");
            out.writeUTF(tag != null ? tag.getSuffix() : "");
            
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao sincronizar tag com BungeeCord: " + e.getMessage());
        }
    }
    
    public void broadcastTagChange(Player player, Tag tag) {
        if (!enabled) {
            return;
        }
        
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("HorusTagChange");
            
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(data);
            
            dataOut.writeUTF(player.getUniqueId().toString());
            dataOut.writeUTF(tag != null ? tag.getId() : "");
            
            out.writeShort(data.toByteArray().length);
            out.write(data.toByteArray());
            
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao fazer broadcast de mudança de tag: " + e.getMessage());
        }
    }
}

