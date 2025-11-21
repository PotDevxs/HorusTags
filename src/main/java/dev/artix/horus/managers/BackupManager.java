package dev.artix.horus.managers;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupManager {
    
    private final Horus plugin;
    
    public BackupManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public boolean createBackup() {
        try {
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File backupFile = new File(backupDir, "tags_backup_" + timestamp + ".yml");
            
            YamlConfiguration backup = new YamlConfiguration();
            
            backup.set("backup-date", System.currentTimeMillis());
            backup.set("backup-version", plugin.getDescription().getVersion());
            
            int tagCount = 0;
            for (Tag tag : plugin.getTagManager().getAllTags()) {
                String path = "tags." + tag.getId();
                backup.set(path + ".id", tag.getId());
                backup.set(path + ".name", tag.getName());
                backup.set(path + ".display-name", tag.getDisplayName());
                backup.set(path + ".prefix", tag.getPrefix());
                backup.set(path + ".suffix", tag.getSuffix());
                backup.set(path + ".description", tag.getDescription());
                backup.set(path + ".category", tag.getCategory());
                backup.set(path + ".rarity", tag.getRarity() != null ? tag.getRarity().name() : null);
                backup.set(path + ".type", tag.getType() != null ? tag.getType().name() : null);
                backup.set(path + ".price", tag.getPrice());
                backup.set(path + ".permission", tag.getPermission());
                backup.set(path + ".duration", tag.getDuration());
                backup.set(path + ".priority", tag.getPriority());
                backup.set(path + ".animated", tag.isAnimated());
                backup.set(path + ".animation-frames", tag.getAnimationFrames());
                backup.set(path + ".animation-speed", tag.getAnimationSpeed());
                backup.set(path + ".glow", tag.isGlow());
                backup.set(path + ".particle-effect", tag.getParticleEffect());
                backup.set(path + ".color", tag.getColor());
                backup.set(path + ".format", tag.getFormat());
                backup.set(path + ".limited", tag.isLimited());
                backup.set(path + ".max-owners", tag.getMaxOwners());
                backup.set(path + ".seasonal", tag.isSeasonal());
                backup.set(path + ".season", tag.getSeason());
                backup.set(path + ".purchasable", tag.isPurchasable());
                backup.set(path + ".tradeable", tag.isTradeable());
                backup.set(path + ".giftable", tag.isGiftable());
                backup.set(path + ".conditions", tag.getConditions() != null ? new java.util.ArrayList<>(tag.getConditions()) : null);
                backup.set(path + ".regions", tag.getRegions() != null ? new java.util.ArrayList<>(tag.getRegions()) : null);
                backup.set(path + ".time-restriction", tag.getTimeRestriction());
                backup.set(path + ".enabled", tag.isEnabled());
                backup.set(path + ".created-at", tag.getCreatedAt());
                tagCount++;
            }
            
            backup.set("tag-count", tagCount);
            backup.save(backupFile);
            
            LoggerUtil.info("Backup criado: " + backupFile.getName() + " (" + tagCount + " tags)");
            return true;
        } catch (IOException e) {
            LoggerUtil.severe("Erro ao criar backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean restoreBackup(File backupFile) {
        if (!backupFile.exists()) {
            return false;
        }
        
        try {
            YamlConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);
            
            if (!backup.contains("tags")) {
                return false;
            }
            
            int restored = 0;
            for (String key : backup.getConfigurationSection("tags").getKeys(false)) {
                String path = "tags." + key;
                Tag tag = new Tag(backup.getString(path + ".id", key), backup.getString(path + ".name", key));
                
                tag.setDisplayName(backup.getString(path + ".display-name"));
                tag.setPrefix(backup.getString(path + ".prefix"));
                tag.setSuffix(backup.getString(path + ".suffix"));
                tag.setDescription(backup.getStringList(path + ".description"));
                tag.setCategory(backup.getString(path + ".category"));
                
                String rarity = backup.getString(path + ".rarity");
                if (rarity != null) {
                    try {
                        tag.setRarity(Tag.TagRarity.valueOf(rarity));
                    } catch (Exception e) {}
                }
                
                String type = backup.getString(path + ".type");
                if (type != null) {
                    try {
                        tag.setType(Tag.TagType.valueOf(type));
                    } catch (Exception e) {}
                }
                
                tag.setPrice(backup.getDouble(path + ".price", 0.0));
                tag.setPermission(backup.getString(path + ".permission"));
                tag.setDuration(backup.getLong(path + ".duration", -1));
                tag.setPriority(backup.getInt(path + ".priority", 0));
                tag.setAnimated(backup.getBoolean(path + ".animated", false));
                tag.setAnimationFrames(backup.getStringList(path + ".animation-frames"));
                tag.setAnimationSpeed(backup.getLong(path + ".animation-speed", 1000));
                tag.setGlow(backup.getBoolean(path + ".glow", false));
                tag.setParticleEffect(backup.getString(path + ".particle-effect"));
                tag.setColor(backup.getString(path + ".color"));
                tag.setFormat(backup.getString(path + ".format"));
                tag.setLimited(backup.getBoolean(path + ".limited", false));
                tag.setMaxOwners(backup.getInt(path + ".max-owners", -1));
                tag.setSeasonal(backup.getBoolean(path + ".seasonal", false));
                tag.setSeason(backup.getString(path + ".season"));
                tag.setPurchasable(backup.getBoolean(path + ".purchasable", false));
                tag.setTradeable(backup.getBoolean(path + ".tradeable", false));
                tag.setGiftable(backup.getBoolean(path + ".giftable", false));
                tag.setConditions(new java.util.HashSet<>(backup.getStringList(path + ".conditions")));
                tag.setRegions(new java.util.HashSet<>(backup.getStringList(path + ".regions")));
                tag.setTimeRestriction(backup.getString(path + ".time-restriction"));
                tag.setEnabled(backup.getBoolean(path + ".enabled", true));
                tag.setCreatedAt(backup.getLong(path + ".created-at", System.currentTimeMillis()));
                
                plugin.getTagManager().saveTag(tag);
                restored++;
            }
            
            LoggerUtil.info("Backup restaurado: " + restored + " tags");
            return true;
        } catch (Exception e) {
            LoggerUtil.severe("Erro ao restaurar backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public java.util.List<File> listBackups() {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            return new java.util.ArrayList<>();
        }
        
        File[] files = backupDir.listFiles((dir, name) -> name.startsWith("tags_backup_") && name.endsWith(".yml"));
        if (files == null) {
            return new java.util.ArrayList<>();
        }
        
        java.util.List<File> backups = new java.util.ArrayList<>(java.util.Arrays.asList(files));
        backups.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        return backups;
    }
}

