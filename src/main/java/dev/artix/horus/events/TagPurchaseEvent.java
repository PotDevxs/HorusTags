package dev.artix.horus.events;

import dev.artix.horus.models.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TagPurchaseEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Tag tag;
    private final double price;
    
    public TagPurchaseEvent(Player player, Tag tag, double price) {
        this.player = player;
        this.tag = tag;
        this.price = price;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Tag getTag() {
        return tag;
    }
    
    public double getPrice() {
        return price;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

