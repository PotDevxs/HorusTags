package dev.artix.horus.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

public class ClickableMessageUtil {
    
    public static void sendClickableTagList(Player player, List<dev.artix.horus.models.Tag> tags, String activeTagId, String commandPrefix) {
        player.sendMessage(ColorUtil.translateColors("&6&l=== Suas Tags Disponíveis ==="));
        player.sendMessage("");
        
        TextComponent message = new TextComponent("");
        boolean first = true;
        
        for (dev.artix.horus.models.Tag tag : tags) {
            if (!first) {
                TextComponent separator = new TextComponent(ColorUtil.translateColors("&7, "));
                message.addExtra(separator);
            }
            first = false;
            
            String displayName = tag.getDisplayName();
            String coloredName = ColorUtil.translateColors(displayName);
            
            boolean isActive = tag.getId().equals(activeTagId);
            String tagText = isActive ? ColorUtil.translateColors("&a[ATIVA] &r" + coloredName) : coloredName;
            
            TextComponent tagComponent = new TextComponent(tagText);
            
            StringBuilder hoverBuilder = new StringBuilder();
            hoverBuilder.append(ColorUtil.translateColors("&6Tag: &f")).append(displayName).append("\n");
            if (isActive) {
                hoverBuilder.append(ColorUtil.translateColors("&a✓ Tag Ativa"));
            } else {
                hoverBuilder.append(ColorUtil.translateColors("&7Clique para equipar"));
            }
            if (tag.getRarity() != null) {
                hoverBuilder.append("\n").append(ColorUtil.translateColors("&7Raridade: &f")).append(tag.getRarity().name());
            }
            if (tag.getDescription() != null && !tag.getDescription().isEmpty()) {
                hoverBuilder.append("\n").append(ColorUtil.translateColors("&7Descrição: &f"));
                for (int i = 0; i < tag.getDescription().size(); i++) {
                    if (i > 0) hoverBuilder.append(" ");
                    hoverBuilder.append(tag.getDescription().get(i));
                }
            }
            
            String hoverText = hoverBuilder.toString();
            tagComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()
            ));
            
            String command = commandPrefix + " " + tag.getId();
            tagComponent.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                command
            ));
            
            message.addExtra(tagComponent);
        }
        
        sendComponentMessage(player, message);
        
        player.sendMessage("");
        player.sendMessage(ColorUtil.translateColors("&7Clique em uma tag acima para equipá-la, ou digite &c'cancelar' &7para cancelar."));
    }
    
    private static void sendComponentMessage(Player player, TextComponent component) {
        try {
            Method spigotMethod = player.getClass().getMethod("spigot");
            Object spigotPlayer = spigotMethod.invoke(player);
            Method sendMessageMethod = spigotPlayer.getClass().getMethod("sendMessage", net.md_5.bungee.api.chat.BaseComponent.class);
            sendMessageMethod.invoke(spigotPlayer, component);
        } catch (Exception e) {
            try {
                Method sendMessageMethod = player.getClass().getMethod("sendMessage", net.md_5.bungee.api.chat.BaseComponent.class);
                sendMessageMethod.invoke(player, component);
            } catch (Exception e2) {
                String fallbackMessage = "";
                for (BaseComponent extra : component.getExtra()) {
                    if (extra != null) {
                        fallbackMessage += ((TextComponent) extra).getText();
                    }
                }
                if (fallbackMessage.isEmpty()) {
                    fallbackMessage = component.getText();
                }
                player.sendMessage(fallbackMessage);
            }
        }
    }
    
    public static void sendClickableTag(Player player, dev.artix.horus.models.Tag tag, String command, String hoverText) {
        String displayName = ColorUtil.translateColors(tag.getDisplayName());
        
        TextComponent tagComponent = new TextComponent(displayName);
        tagComponent.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(ColorUtil.translateColors(hoverText)).create()
        ));
        tagComponent.setClickEvent(new ClickEvent(
            ClickEvent.Action.RUN_COMMAND,
            command
        ));
        
        sendComponentMessage(player, tagComponent);
    }
}
