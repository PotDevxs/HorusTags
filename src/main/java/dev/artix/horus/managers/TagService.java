package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagService {
    
    private final Horus plugin;
    private final Map<UUID, Integer> particleTasks;
    
    public TagService(Horus plugin) {
        this.plugin = plugin;
        this.particleTasks = new HashMap<>();
        startAnimationUpdater();
    }
    
    public String formatTag(Player player, Tag tag) {
        if (tag == null) {
            return "";
        }
        
        String prefix = tag.getPrefix();
        String suffix = tag.getSuffix();
        
        if (prefix != null) {
            prefix = ColorUtil.translateColors(prefix);
            prefix = replacePlaceholders(player, prefix);
        }
        
        if (suffix != null) {
            suffix = ColorUtil.translateColors(suffix);
            suffix = replacePlaceholders(player, suffix);
        }
        
        if (tag.isAnimated() && tag.getAnimationFrames() != null && !tag.getAnimationFrames().isEmpty()) {
            String animatedFrame = getAnimatedFrame(tag);
            animatedFrame = ColorUtil.translateColors(animatedFrame);
            animatedFrame = replacePlaceholders(player, animatedFrame);
            return (prefix != null ? prefix : "") + animatedFrame + (suffix != null ? suffix : "");
        }
        
        return (prefix != null ? prefix : "") + (suffix != null ? suffix : "");
    }
    
    private String getAnimatedFrame(Tag tag) {
        if (tag.getAnimationFrames() == null || tag.getAnimationFrames().isEmpty()) {
            return "";
        }
        
        String animationType = tag.getAnimationType() != null ? tag.getAnimationType().toLowerCase() : "frame";
        
        switch (animationType) {
            case "gradient":
                return getGradientAnimation(tag);
            case "rainbow":
                return getRainbowAnimation(tag);
            case "fade":
                return getFadeAnimation(tag);
            case "wave":
                return getWaveAnimation(tag);
            case "frame":
            default:
                return getFrameAnimation(tag);
        }
    }
    
    private String getFrameAnimation(Tag tag) {
        long currentTime = System.currentTimeMillis();
        long frameIndex = (currentTime / tag.getAnimationSpeed()) % tag.getAnimationFrames().size();
        return tag.getAnimationFrames().get((int) frameIndex);
    }
    
    private String getGradientAnimation(Tag tag) {
        if (tag.getAnimationFrames().size() < 2) {
            return getFrameAnimation(tag);
        }
        
        long currentTime = System.currentTimeMillis();
        long cycleTime = tag.getAnimationSpeed() * tag.getAnimationFrames().size();
        double progress = (currentTime % cycleTime) / (double) cycleTime;
        
        int frameIndex = (int) (progress * tag.getAnimationFrames().size());
        if (frameIndex >= tag.getAnimationFrames().size()) {
            frameIndex = tag.getAnimationFrames().size() - 1;
        }
        
        return tag.getAnimationFrames().get(frameIndex);
    }
    
    private String getRainbowAnimation(Tag tag) {
        String baseText = tag.getAnimationFrames().isEmpty() ? tag.getDisplayName() : tag.getAnimationFrames().get(0);
        long currentTime = System.currentTimeMillis();
        double hue = (currentTime / (double) tag.getAnimationSpeed()) % 1.0;
        
        StringBuilder result = new StringBuilder();
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&d"};
        
        for (int i = 0; i < baseText.length(); i++) {
            int colorIndex = (int) ((hue * 6 + i * 0.1) % colors.length);
            result.append(colors[colorIndex]).append(baseText.charAt(i));
        }
        
        return result.toString();
    }
    
    private String getFadeAnimation(Tag tag) {
        String baseText = tag.getAnimationFrames().isEmpty() ? tag.getDisplayName() : tag.getAnimationFrames().get(0);
        long currentTime = System.currentTimeMillis();
        double fade = Math.abs(Math.sin((currentTime / (double) tag.getAnimationSpeed()) * Math.PI));
        
        int alpha = (int) (fade * 15);
        String hexAlpha = Integer.toHexString(alpha);
        
        return "&" + hexAlpha + baseText;
    }
    
    private String getWaveAnimation(Tag tag) {
        String baseText = tag.getAnimationFrames().isEmpty() ? tag.getDisplayName() : tag.getAnimationFrames().get(0);
        long currentTime = System.currentTimeMillis();
        double wave = Math.sin((currentTime / (double) tag.getAnimationSpeed()) * Math.PI * 2);
        
        StringBuilder result = new StringBuilder();
        String[] colors = {"&f", "&7", "&8", "&7", "&f"};
        
        for (int i = 0; i < baseText.length(); i++) {
            double offset = wave + (i * 0.2);
            int colorIndex = (int) ((offset * 2 + 2) % colors.length);
            if (colorIndex < 0) colorIndex += colors.length;
            result.append(colors[colorIndex]).append(baseText.charAt(i));
        }
        
        return result.toString();
    }
    
    private String replacePlaceholders(Player player, String text) {
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
    
    public void applyTagEffects(Player player, Tag tag) {
        if (tag.isGlow()) {
            applyGlowEffect(player);
        }
        
        if (tag.getParticleEffect() != null) {
            applyParticleEffect(player, tag.getParticleEffect());
        }
    }
    
    private void applyGlowEffect(Player player) {
        try {
            Method setGlowingMethod = player.getClass().getMethod("setGlowing", boolean.class);
            setGlowingMethod.invoke(player, true);
        } catch (Exception e) {
        }
    }
    
    private void applyParticleEffect(Player player, String effect) {
        try {
            Class<?> particleEnum = Class.forName("org.bukkit.Particle");
            Object particle = Enum.valueOf((Class<Enum>) particleEnum, effect.toUpperCase());
            
            Class<?> worldClass = player.getWorld().getClass();
            Method spawnParticleMethod = worldClass.getMethod("spawnParticle", 
                particleEnum, 
                org.bukkit.Location.class, 
                int.class, 
                double.class, 
                double.class, 
                double.class, 
                double.class);
            
            spawnParticleMethod.invoke(player.getWorld(), 
                particle, 
                player.getLocation().add(0, 2, 0), 
                10, 
                0.5, 
                0.5, 
                0.5, 
                0.1);
        } catch (Exception e) {
            try {
                Class<?> effectEnum = Class.forName("org.bukkit.Effect");
                Object effectObj = Enum.valueOf((Class<Enum>) effectEnum, effect.toUpperCase());
                
                player.getWorld().playEffect(
                    player.getLocation().add(0, 2, 0),
                    (org.bukkit.Effect) effectObj,
                    0
                );
            } catch (Exception e2) {
            }
        }
    }
    
    public void updatePlayerTag(Player player) {
        String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
        if (activeTagId == null) {
            stopParticleEffect(player);
            plugin.getTitleManager().stopTitleAnimation(player);
            return;
        }
        
        Tag tag = plugin.getTagManager().getTag(activeTagId);
        if (tag == null) {
            stopParticleEffect(player);
            plugin.getTitleManager().stopTitleAnimation(player);
            return;
        }
        
        String formattedTag = formatTag(player, tag);
        applyTagEffects(player, tag);
        
        if (plugin.getIntegrationManager().isPlaceholderAPIEnabled()) {
            dev.artix.horus.integrations.PlaceholderAPIIntegration papi = plugin.getIntegrationManager().getPlaceholderAPI();
            if (papi != null) {
                formattedTag = papi.replacePlaceholders(player, formattedTag);
            }
        }
        
        if (tag.isAnimated() || tag.getParticleEffect() != null) {
            startParticleEffect(player, tag);
        } else {
            stopParticleEffect(player);
        }
        
        if (plugin.getTitleManager().getTitleConfig(tag.getId()) != null) {
            plugin.getTitleManager().startTitleAnimation(player, tag);
        } else {
            plugin.getTitleManager().stopTitleAnimation(player);
        }
    }
    
    private void startAnimationUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String activeTagId = plugin.getPlayerTagManager().getActiveTag(player.getUniqueId());
                    if (activeTagId == null) continue;
                    
                    Tag tag = plugin.getTagManager().getTag(activeTagId);
                    if (tag == null || !tag.isAnimated()) continue;
                    
                    plugin.getDisplayNameManager().updatePlayerDisplayName(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    private void startParticleEffect(Player player, Tag tag) {
        if (tag.getParticleEffect() == null) {
            return;
        }
        
        stopParticleEffect(player);
        
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
                    particleTasks.remove(player.getUniqueId());
                    return;
                }
                
                applyParticleEffect(player, tag.getParticleEffect());
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        
        particleTasks.put(player.getUniqueId(), taskId);
    }
    
    private void stopParticleEffect(Player player) {
        Integer taskId = particleTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}

