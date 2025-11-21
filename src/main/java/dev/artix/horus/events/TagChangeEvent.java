package dev.artix.horus.events;

import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TagChangeEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Tag newTag;
    private final Tag oldTag;
    
    public TagChangeEvent(Player player, Tag newTag, Tag oldTag) {
        this.player = player;
        this.newTag = newTag;
        this.oldTag = oldTag;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Tag getNewTag() {
        return newTag;
    }
    
    public Tag getOldTag() {
        return oldTag;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

