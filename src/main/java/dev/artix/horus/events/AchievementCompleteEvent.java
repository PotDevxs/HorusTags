package dev.artix.horus.events;

import dev.artix.horus.models.Achievement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AchievementCompleteEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Achievement achievement;
    
    public AchievementCompleteEvent(Player player, Achievement achievement) {
        this.player = player;
        this.achievement = achievement;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Achievement getAchievement() {
        return achievement;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

