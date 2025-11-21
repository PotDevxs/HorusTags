package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TagManager {
    
    private final Horus plugin;
    private final Map<String, Tag> tags;
    private final Map<String, List<Tag>> tagsByCategory;
    
    public TagManager(Horus plugin) {
        this.plugin = plugin;
        this.tags = new ConcurrentHashMap<>();
        this.tagsByCategory = new ConcurrentHashMap<>();
    }
    
    public void loadTags() {
        tags.clear();
        tagsByCategory.clear();
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config.getBoolean("load-from-database", true)) {
            loadTagsFromDatabase();
        } else {
            loadTagsFromConfig();
        }
        
        LoggerUtil.info("Carregadas " + tags.size() + " tags");
    }
    
    private void loadTagsFromDatabase() {
        List<Tag> allTags = plugin.getDatabaseManager().getAdapter().getAllTags();
        for (Tag tag : allTags) {
            if (tag.isEnabled()) {
                tags.put(tag.getId(), tag);
                
                String category = tag.getCategory();
                if (category != null) {
                    tagsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(tag);
                }
            }
        }
    }
    
    private void loadTagsFromConfig() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config.contains("tags")) {
            loadTagsFromFile(config, "tags");
        }
        
        FileConfiguration animadas = plugin.getConfigManager().getAnimadas();
        if (animadas.contains("tags")) {
            loadTagsFromFile(animadas, "tags");
        }
    }
    
    private void loadTagsFromFile(FileConfiguration fileConfig, String rootPath) {
        if (!fileConfig.contains(rootPath)) return;
        
        for (String key : fileConfig.getConfigurationSection(rootPath).getKeys(false)) {
            String path = rootPath + "." + key;
            Tag tag = new Tag(key, fileConfig.getString(path + ".name", key));
            
            tag.setDisplayName(fileConfig.getString(path + ".display-name", tag.getName()));
            tag.setPrefix(fileConfig.getString(path + ".prefix", ""));
            tag.setSuffix(fileConfig.getString(path + ".suffix", ""));
            tag.setDescription(fileConfig.getStringList(path + ".description"));
            tag.setCategory(fileConfig.getString(path + ".category", "default"));
            tag.setRarity(Tag.TagRarity.valueOf(fileConfig.getString(path + ".rarity", "COMMON").toUpperCase()));
            tag.setType(Tag.TagType.valueOf(fileConfig.getString(path + ".type", "NORMAL").toUpperCase()));
            tag.setPrice(fileConfig.getDouble(path + ".price", 0.0));
            tag.setPermission(fileConfig.getString(path + ".permission"));
            tag.setRequiredGroups(new HashSet<>(fileConfig.getStringList(path + ".required-groups")));
            tag.setRequiredAchievements(new HashSet<>(fileConfig.getStringList(path + ".required-achievements")));
            tag.setDuration(fileConfig.getLong(path + ".duration", -1));
            tag.setPriority(fileConfig.getInt(path + ".priority", 0));
            tag.setAnimated(fileConfig.getBoolean(path + ".animated", false));
            tag.setAnimationFrames(fileConfig.getStringList(path + ".animation-frames"));
            tag.setAnimationSpeed(fileConfig.getLong(path + ".animation-speed", 1000));
            tag.setAnimationType(fileConfig.getString(path + ".animation-type", "frame"));
            tag.setGlow(fileConfig.getBoolean(path + ".glow", false));
            tag.setTitle(fileConfig.getString(path + ".title"));
            tag.setSubtitle(fileConfig.getString(path + ".subtitle"));
            tag.setTitleAnimated(fileConfig.getBoolean(path + ".title-animated", false));
            tag.setTitleAnimationFrames(fileConfig.getStringList(path + ".title-animation-frames"));
            tag.setTitleAnimationSpeed(fileConfig.getLong(path + ".title-animation-speed", 2000));
            tag.setSubtitleAnimated(fileConfig.getBoolean(path + ".subtitle-animated", false));
            tag.setSubtitleAnimationFrames(fileConfig.getStringList(path + ".subtitle-animation-frames"));
            tag.setSubtitleAnimationSpeed(fileConfig.getLong(path + ".subtitle-animation-speed", 2000));
            tag.setParticleEffect(fileConfig.getString(path + ".particle-effect"));
            tag.setColor(fileConfig.getString(path + ".color"));
            tag.setFormat(fileConfig.getString(path + ".format"));
            tag.setLimited(fileConfig.getBoolean(path + ".limited", false));
            tag.setMaxOwners(fileConfig.getInt(path + ".max-owners", -1));
            tag.setSeasonal(fileConfig.getBoolean(path + ".seasonal", false));
            tag.setSeason(fileConfig.getString(path + ".season"));
            tag.setPurchasable(fileConfig.getBoolean(path + ".purchasable", false));
            tag.setTradeable(fileConfig.getBoolean(path + ".tradeable", false));
            tag.setGiftable(fileConfig.getBoolean(path + ".giftable", false));
            tag.setConditions(new HashSet<>(fileConfig.getStringList(path + ".conditions")));
            tag.setRegions(new HashSet<>(fileConfig.getStringList(path + ".regions")));
            tag.setTimeRestriction(fileConfig.getString(path + ".time-restriction"));
            tag.setEnabled(fileConfig.getBoolean(path + ".enabled", true));
            
            tags.put(tag.getId(), tag);
            
            String category = tag.getCategory();
            if (category != null) {
                tagsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(tag);
            }
        }
    }
    
    public Tag getTag(String id) {
        return tags.get(id);
    }
    
    public Collection<Tag> getAllTags() {
        return tags.values();
    }
    
    public List<Tag> getTagsByCategory(String category) {
        return tagsByCategory.getOrDefault(category, new ArrayList<>());
    }
    
    public List<Tag> getTagsByRarity(Tag.TagRarity rarity) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags.values()) {
            if (tag.getRarity() == rarity) {
                result.add(tag);
            }
        }
        return result;
    }
    
    public List<Tag> getTagsByType(Tag.TagType type) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags.values()) {
            if (tag.getType() == type) {
                result.add(tag);
            }
        }
        return result;
    }
    
    public List<Tag> getPurchasableTags() {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags.values()) {
            if (tag.isPurchasable() && tag.isEnabled()) {
                result.add(tag);
            }
        }
        return result;
    }
    
    public void saveTag(Tag tag) {
        tags.put(tag.getId(), tag);
        saveTagToDatabase(tag);
    }
    
    private void saveTagToDatabase(Tag tag) {
        plugin.getDatabaseManager().getAdapter().saveTag(tag);
    }
    
    public void deleteTag(String id) {
        tags.remove(id);
        plugin.getDatabaseManager().getAdapter().deleteTag(id);
    }
}

