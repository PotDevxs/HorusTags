package dev.artix.horus.config;

import dev.artix.horus.Horus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    
    private final Horus plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration database;
    private FileConfiguration animadas;
    private FileConfiguration titulos;
    
    public ConfigManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        saveResource("messages.yml", false);
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        saveResource("database.yml", false);
        File databaseFile = new File(plugin.getDataFolder(), "database.yml");
        database = YamlConfiguration.loadConfiguration(databaseFile);
        
        saveResource("animadas.yml", false);
        File animadasFile = new File(plugin.getDataFolder(), "animadas.yml");
        animadas = YamlConfiguration.loadConfiguration(animadasFile);
        
        saveResource("titulos.yml", false);
        File titulosFile = new File(plugin.getDataFolder(), "titulos.yml");
        titulos = YamlConfiguration.loadConfiguration(titulosFile);
        
        updateConfigs();
    }
    
    private void saveResource(String resource, boolean replace) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        File file = new File(plugin.getDataFolder(), resource);
        if (!file.exists() || replace) {
            plugin.saveResource(resource, replace);
        }
    }
    
    private void updateConfigs() {
        InputStream defaultConfig = plugin.getResource("config.yml");
        if (defaultConfig != null) {
            YamlConfiguration defaultConfigFile = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultConfig, StandardCharsets.UTF_8));
            
            boolean updated = false;
            for (String key : defaultConfigFile.getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defaultConfigFile.get(key));
                    updated = true;
                }
            }
            
            if (updated) {
                try {
                    config.save(new File(plugin.getDataFolder(), "config.yml"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getMessages() {
        return messages;
    }
    
    public FileConfiguration getDatabase() {
        return database;
    }
    
    public FileConfiguration getAnimadas() {
        return animadas;
    }
    
    public FileConfiguration getTitulos() {
        return titulos;
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        File databaseFile = new File(plugin.getDataFolder(), "database.yml");
        database = YamlConfiguration.loadConfiguration(databaseFile);
        
        File animadasFile = new File(plugin.getDataFolder(), "animadas.yml");
        animadas = YamlConfiguration.loadConfiguration(animadasFile);
        
        File titulosFile = new File(plugin.getDataFolder(), "titulos.yml");
        titulos = YamlConfiguration.loadConfiguration(titulosFile);
    }
}

