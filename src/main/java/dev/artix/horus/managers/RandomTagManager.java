package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RandomTagManager {
    
    private final Horus plugin;
    private final Map<String, List<String>> dailyTags;
    private String currentDay;
    
    public RandomTagManager(Horus plugin) {
        this.plugin = plugin;
        this.dailyTags = new HashMap<>();
        this.currentDay = getCurrentDay();
        startDailyRotation();
    }
    
    public void registerDailyTag(String category, String tagId) {
        dailyTags.computeIfAbsent(category, k -> new ArrayList<>()).add(tagId);
    }
    
    public List<Tag> getDailyTags(String category) {
        List<Tag> tags = new ArrayList<>();
        List<String> tagIds = dailyTags.get(category);
        
        if (tagIds == null || tagIds.isEmpty()) {
            return tags;
        }
        
        Random random = new Random();
        int count = Math.min(3, tagIds.size());
        Collections.shuffle(tagIds, random);
        
        for (int i = 0; i < count; i++) {
            String tagId = tagIds.get(i);
            Tag tag = plugin.getTagManager().getTag(tagId);
            if (tag != null && tag.isEnabled()) {
                tags.add(tag);
            }
        }
        
        return tags;
    }
    
    public void giveRandomDailyTag(Player player, String category) {
        List<Tag> dailyTags = getDailyTags(category);
        if (dailyTags.isEmpty()) {
            return;
        }
        
        Random random = new Random();
        Tag randomTag = dailyTags.get(random.nextInt(dailyTags.size()));
        
        if (!plugin.getPlayerTagManager().hasTag(player.getUniqueId(), randomTag.getId())) {
            plugin.getPlayerTagManager().giveTag(player.getUniqueId(), randomTag.getId(), "DAILY");
            player.sendMessage(ColorUtil.translateColors("&6&lTag Diária!"));
            player.sendMessage(ColorUtil.translateColors("&7Você recebeu a tag: &f" + randomTag.getDisplayName()));
        }
    }
    
    private String getCurrentDay() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
    }
    
    private void startDailyRotation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String today = getCurrentDay();
                if (!today.equals(currentDay)) {
                    currentDay = today;
                    notifyDailyTags();
                }
            }
        }.runTaskTimer(plugin, 0L, 72000L);
    }
    
    private void notifyDailyTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfigManager().getConfig().getBoolean("daily-tags.notify", true)) {
                player.sendMessage(ColorUtil.translateColors("&6&lNovas Tags Diárias Disponíveis!"));
                player.sendMessage(ColorUtil.translateColors("&7Use &e/tag daily &7para receber uma tag aleatória!"));
            }
        }
    }
}

