package dev.artix.horus.database;

import dev.artix.horus.Horus;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseManager {
    
    private final Horus plugin;
    private DatabaseAdapter adapter;
    private DatabaseType databaseType;
    
    public DatabaseManager(Horus plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        FileConfiguration dbConfig = plugin.getConfigManager().getDatabase();
        String type = dbConfig.getString("type", "sqlite").toLowerCase();
        
        switch (type) {
            case "mysql":
            case "sqlite":
                databaseType = type.equals("mysql") ? DatabaseType.MYSQL : DatabaseType.SQLITE;
                SQLAdapter sqlAdapter = new SQLAdapter(plugin);
                sqlAdapter.initialize(databaseType);
                adapter = sqlAdapter;
                LoggerUtil.info("Database SQL inicializado: " + databaseType.name());
                break;
            case "mongodb":
                databaseType = DatabaseType.MONGODB;
                adapter = new MongoDBAdapter(plugin);
                adapter.initialize();
                LoggerUtil.info("Database MongoDB inicializado");
                break;
            case "flatfile":
            case "flat-file":
            case "file":
                databaseType = DatabaseType.FLATFILE;
                adapter = new FlatFileAdapter(plugin);
                adapter.initialize();
                LoggerUtil.info("Database Flat-File inicializado");
                break;
            default:
                LoggerUtil.severe("Tipo de database desconhecido: " + type + ". Usando SQLite como padrão.");
                databaseType = DatabaseType.SQLITE;
                SQLAdapter defaultAdapter = new SQLAdapter(plugin);
                defaultAdapter.initialize(DatabaseType.SQLITE);
                adapter = defaultAdapter;
                break;
        }
    }
    
    public DatabaseAdapter getAdapter() {
        return adapter;
    }
    
    public DatabaseType getDatabaseType() {
        return databaseType;
    }
    
    public void close() {
        if (adapter != null) {
            adapter.close();
        }
    }
    
    public enum DatabaseType {
        MYSQL, SQLITE, MONGODB, FLATFILE
    }
}

