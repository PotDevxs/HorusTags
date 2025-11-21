package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.models.TitleConfig;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TitleManager {
    
    private final Horus plugin;
    private final Map<UUID, Integer> titleTasks;
    private final Map<String, TitleConfig> titleConfigs;
    
    public TitleManager(Horus plugin) {
        this.plugin = plugin;
        this.titleTasks = new HashMap<>();
        this.titleConfigs = new ConcurrentHashMap<>();
        loadTitleConfigs();
    }
    
    public void loadTitleConfigs() {
        titleConfigs.clear();
        
        FileConfiguration titulosConfig = plugin.getConfigManager().getTitulos();
        if (!titulosConfig.contains("titles")) {
            return;
        }
        
        for (String tagId : titulosConfig.getConfigurationSection("titles").getKeys(false)) {
            String path = "titles." + tagId;
            TitleConfig titleConfig = new TitleConfig();
            
            titleConfig.setTitle(titulosConfig.getString(path + ".title"));
            titleConfig.setSubtitle(titulosConfig.getString(path + ".subtitle"));
            titleConfig.setTitleAnimated(titulosConfig.getBoolean(path + ".title-animated", false));
            titleConfig.setTitleAnimationFrames(titulosConfig.getStringList(path + ".title-animation-frames"));
            titleConfig.setTitleAnimationSpeed(titulosConfig.getLong(path + ".title-animation-speed", 2000));
            titleConfig.setSubtitleAnimated(titulosConfig.getBoolean(path + ".subtitle-animated", false));
            titleConfig.setSubtitleAnimationFrames(titulosConfig.getStringList(path + ".subtitle-animation-frames"));
            titleConfig.setSubtitleAnimationSpeed(titulosConfig.getLong(path + ".subtitle-animation-speed", 2000));
            titleConfig.setFadeIn(titulosConfig.getInt(path + ".fade-in", 10));
            titleConfig.setStay(titulosConfig.getInt(path + ".stay", 40));
            titleConfig.setFadeOut(titulosConfig.getInt(path + ".fade-out", 10));
            
            titleConfigs.put(tagId, titleConfig);
        }
    }
    
    public TitleConfig getTitleConfig(String tagId) {
        return titleConfigs.get(tagId);
    }
    
    public void reloadTitleConfigs() {
        loadTitleConfigs();
    }
    
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Object titlePacket = createTitlePacket(title, subtitle, fadeIn, stay, fadeOut);
            sendPacket(player, titlePacket);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao enviar título: " + e.getMessage());
        }
    }
    
    public void sendActionBar(Player player, String message) {
        try {
            Object actionBarPacket = createActionBarPacket(message);
            sendPacket(player, actionBarPacket);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao enviar action bar: " + e.getMessage());
        }
    }
    
    public void startTitleAnimation(Player player, Tag tag) {
        if (tag == null) {
            stopTitleAnimation(player);
            return;
        }
        
        TitleConfig titleConfig = getTitleConfig(tag.getId());
        if (titleConfig == null || titleConfig.getTitle() == null) {
            stopTitleAnimation(player);
            return;
        }
        
        stopTitleAnimation(player);
        
        long animationSpeed = titleConfig.getTitleAnimationSpeed() > 0 ? titleConfig.getTitleAnimationSpeed() : 2000L;
        
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
                if (activeTagId == null || !activeTagId.equals(tag.getId())) {
                    cancel();
                    titleTasks.remove(player.getUniqueId());
                    return;
                }
                
                TitleConfig currentConfig = getTitleConfig(tag.getId());
                if (currentConfig == null) {
                    cancel();
                    titleTasks.remove(player.getUniqueId());
                    return;
                }
                
                String animatedTitle = currentConfig.getTitle();
                String animatedSubtitle = currentConfig.getSubtitle();
                
                if (currentConfig.isTitleAnimated() && currentConfig.getTitleAnimationFrames() != null && !currentConfig.getTitleAnimationFrames().isEmpty()) {
                    animatedTitle = getAnimatedTitleFrame(currentConfig);
                }
                
                if (currentConfig.isSubtitleAnimated() && currentConfig.getSubtitleAnimationFrames() != null && !currentConfig.getSubtitleAnimationFrames().isEmpty()) {
                    animatedSubtitle = getAnimatedSubtitleFrame(currentConfig);
                }
                
                animatedTitle = ColorUtil.translateColors(animatedTitle);
                animatedSubtitle = ColorUtil.translateColors(animatedSubtitle);
                
                animatedTitle = replacePlaceholders(player, animatedTitle);
                animatedSubtitle = replacePlaceholders(player, animatedSubtitle);
                
                sendTitle(player, animatedTitle, animatedSubtitle, currentConfig.getFadeIn(), currentConfig.getStay(), currentConfig.getFadeOut());
            }
        }.runTaskTimer(plugin, 0L, animationSpeed / 50L).getTaskId();
        
        titleTasks.put(player.getUniqueId(), taskId);
    }
    
    public void stopTitleAnimation(Player player) {
        Integer taskId = titleTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
    
    private String getAnimatedTitleFrame(TitleConfig titleConfig) {
        long currentTime = System.currentTimeMillis();
        long frameIndex = (currentTime / titleConfig.getTitleAnimationSpeed()) % titleConfig.getTitleAnimationFrames().size();
        return titleConfig.getTitleAnimationFrames().get((int) frameIndex);
    }
    
    private String getAnimatedSubtitleFrame(TitleConfig titleConfig) {
        long currentTime = System.currentTimeMillis();
        long frameIndex = (currentTime / titleConfig.getSubtitleAnimationSpeed()) % titleConfig.getSubtitleAnimationFrames().size();
        return titleConfig.getSubtitleAnimationFrames().get((int) frameIndex);
    }
    
    private String replacePlaceholders(Player player, String text) {
        if (text == null) return "";
        
        text = text.replace("{player}", player.getName());
        text = text.replace("{displayname}", player.getDisplayName());
        
        if (plugin.getIntegrationManager().isPlaceholderAPIEnabled()) {
            dev.artix.horus.integrations.PlaceholderAPIIntegration papi = plugin.getIntegrationManager().getPlaceholderAPI();
            if (papi != null) {
                text = papi.replacePlaceholders(player, text);
            }
        }
        
        return text;
    }
    
    private Object createTitlePacket(String title, String subtitle, int fadeIn, int stay, int fadeOut) throws Exception {
        Class<?> chatComponentClass = getNMSClass("IChatBaseComponent");
        Class<?> chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");
        Class<?> packetClass = getNMSClass("PacketPlayOutTitle");
        Class<?> actionClass = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
        
        Object titleComponent = null;
        Object subtitleComponent = null;
        
        if (title != null && !title.isEmpty()) {
            Method a = chatSerializerClass.getMethod("a", String.class);
            titleComponent = a.invoke(null, "{\"text\":\"" + title.replace("\"", "\\\"") + "\"}");
        }
        
        if (subtitle != null && !subtitle.isEmpty()) {
            Method a = chatSerializerClass.getMethod("a", String.class);
            subtitleComponent = a.invoke(null, "{\"text\":\"" + subtitle.replace("\"", "\\\"") + "\"}");
        }
        
        Object titlePacket = null;
        Object subtitlePacket = null;
        
        if (titleComponent != null) {
            Constructor<?> titleConstructor = packetClass.getConstructor(actionClass, chatComponentClass, int.class, int.class, int.class);
            Object titleAction = actionClass.getEnumConstants()[0];
            titlePacket = titleConstructor.newInstance(titleAction, titleComponent, fadeIn, stay, fadeOut);
        }
        
        if (subtitleComponent != null) {
            Constructor<?> subtitleConstructor = packetClass.getConstructor(actionClass, chatComponentClass, int.class, int.class, int.class);
            Object subtitleAction = actionClass.getEnumConstants()[1];
            subtitlePacket = subtitleConstructor.newInstance(subtitleAction, subtitleComponent, fadeIn, stay, fadeOut);
        }
        
        return new Object[]{titlePacket, subtitlePacket};
    }
    
    private Object createActionBarPacket(String message) throws Exception {
        Class<?> chatComponentClass = getNMSClass("IChatBaseComponent");
        Class<?> chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");
        Class<?> packetClass = getNMSClass("PacketPlayOutChat");
        
        Method a = chatSerializerClass.getMethod("a", String.class);
        Object component = a.invoke(null, "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}");
        
        Constructor<?> constructor = packetClass.getConstructor(chatComponentClass, byte.class);
        return constructor.newInstance(component, (byte) 2);
    }
    
    private void sendPacket(Player player, Object packet) throws Exception {
        if (packet instanceof Object[]) {
            Object[] packets = (Object[]) packet;
            for (Object p : packets) {
                if (p != null) {
                    sendSinglePacket(player, p);
                }
            }
        } else {
            sendSinglePacket(player, packet);
        }
    }
    
    private void sendSinglePacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
        sendPacket.invoke(playerConnection, packet);
    }
    
    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }
}

