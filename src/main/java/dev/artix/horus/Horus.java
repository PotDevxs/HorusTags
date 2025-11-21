package dev.artix.horus;

import dev.artix.horus.commands.CommandArg;
import dev.artix.horus.commands.CommandManager;
import dev.artix.horus.commands.TagCommand;
import dev.artix.horus.config.ConfigManager;
import dev.artix.horus.database.DatabaseManager;
import dev.artix.horus.integrations.IntegrationManager;
import dev.artix.horus.listeners.ChatListener;
import dev.artix.horus.listeners.PlayerListener;
import dev.artix.horus.managers.*;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class Horus extends JavaPlugin {

    private static Horus instance;
    
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private IntegrationManager integrationManager;
    
    private TagManager tagManager;
    private PlayerTagManager playerTagManager;
    private EconomyManager economyManager;
    private AchievementManager achievementManager;
    private PermissionManager permissionManager;
    private NotificationManager notificationManager;
    private CacheManager cacheManager;
    private GUIManager guiManager;
    private TagService tagService;
    private ChatSelectionManager chatSelectionManager;
    private CommandManager commandManager;
    private TradeManager tradeManager;
    private GiftManager giftManager;
    private ConditionManager conditionManager;
    private DisplayNameManager displayNameManager;
    private DynamicTagManager dynamicTagManager;
    private BackupManager backupManager;
    private LogManager logManager;
    private StatsManager statsManager;
    private RandomTagManager randomTagManager;
    private CollectionManager collectionManager;
    private TitleManager titleManager;

    @Override
    public void onEnable() {
        instance = this;
        
        LoggerUtil.info("Inicializando HorusTags Plugin...");
        
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        integrationManager = new IntegrationManager(this);
        integrationManager.loadIntegrations();
        
        tagManager = new TagManager(this);
        playerTagManager = new PlayerTagManager(this);
        economyManager = new EconomyManager(this);
        achievementManager = new AchievementManager(this);
        permissionManager = new PermissionManager(this);
        notificationManager = new NotificationManager(this);
        cacheManager = new CacheManager(this);
        guiManager = new GUIManager(this);
        tagService = new TagService(this);
        chatSelectionManager = new ChatSelectionManager(this);
        commandManager = new CommandManager(this);
        tradeManager = new TradeManager(this);
        giftManager = new GiftManager(this);
        conditionManager = new ConditionManager(this);
        displayNameManager = new DisplayNameManager(this);
        dynamicTagManager = new DynamicTagManager(this);
        backupManager = new BackupManager(this);
        logManager = new LogManager(this);
        statsManager = new StatsManager(this);
        randomTagManager = new RandomTagManager(this);
        collectionManager = new CollectionManager(this);
        titleManager = new TitleManager(this);
        
        registerCommands();
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        tagManager.loadTags();
        achievementManager.loadAchievements();
        
        LoggerUtil.info("HorusTags Plugin carregado com sucesso!");
    }

    @Override
    public void onDisable() {
        LoggerUtil.info("Desabilitando HorusTags Plugin...");
        
        if (guiManager != null) {
            guiManager.closeAllGUIs();
        }
        
        if (cacheManager != null) {
            cacheManager.saveCache();
        }
        
        if (commandManager != null) {
            commandManager.unregisterAllCommands();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        LoggerUtil.info("HorusTags Plugin desabilitado!");
    }
    
    private void registerCommands() {
        TagCommand tagCommand = new TagCommand(this);
        
        CommandArg tagCommandArg = CommandArg.builder("tag")
                .description("Comando principal de tags")
                .usage("/<command> [subcommand]")
                .aliases("tags", "horustag")
                .permission("horus.use")
                .executor(tagCommand)
                .tabCompleter(tagCommand)
                .build();
        
        commandManager.registerCommand(tagCommandArg);
    }

    public static Horus getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public PlayerTagManager getPlayerTagManager() {
        return playerTagManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public TagService getTagService() {
        return tagService;
    }

    public ChatSelectionManager getChatSelectionManager() {
        return chatSelectionManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public GiftManager getGiftManager() {
        return giftManager;
    }

    public ConditionManager getConditionManager() {
        return conditionManager;
    }

    public DisplayNameManager getDisplayNameManager() {
        return displayNameManager;
    }

    public DynamicTagManager getDynamicTagManager() {
        return dynamicTagManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public RandomTagManager getRandomTagManager() {
        return randomTagManager;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }
    
    public TitleManager getTitleManager() {
        return titleManager;
    }
}
