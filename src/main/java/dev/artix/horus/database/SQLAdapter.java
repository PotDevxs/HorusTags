package dev.artix.horus.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.artix.horus.Horus;
import dev.artix.horus.models.Achievement;
import dev.artix.horus.models.PlayerAchievement;
import dev.artix.horus.models.PlayerTag;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLAdapter implements DatabaseAdapter {
    
    private final Horus plugin;
    private HikariDataSource dataSource;
    private DatabaseManager.DatabaseType databaseType;
    
    public SQLAdapter(Horus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        FileConfiguration dbConfig = plugin.getConfigManager().getDatabase();
        String type = dbConfig.getString("type", "sqlite").toLowerCase();
        
        DatabaseManager.DatabaseType databaseType;
        if (type.equals("mysql")) {
            databaseType = DatabaseManager.DatabaseType.MYSQL;
        } else {
            databaseType = DatabaseManager.DatabaseType.SQLITE;
        }
        
        initialize(databaseType);
    }
    
    public void initialize(DatabaseManager.DatabaseType type) {
        this.databaseType = type;
        
        if (type == DatabaseManager.DatabaseType.MYSQL) {
            initializeMySQL();
        } else {
            initializeSQLite();
        }
        
        createTables();
    }
    
    private void initializeMySQL() {
        FileConfiguration dbConfig = plugin.getConfigManager().getDatabase();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbConfig.getString("mysql.host") + ":" + 
                         dbConfig.getInt("mysql.port") + "/" + dbConfig.getString("mysql.database"));
        config.setUsername(dbConfig.getString("mysql.username"));
        config.setPassword(dbConfig.getString("mysql.password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    private void initializeSQLite() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + plugin.getDataFolder().getAbsolutePath() + "/database");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        
        dataSource = new HikariDataSource(config);
    }
    
    private void createTables() {
        try (Connection connection = getConnection()) {
            if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                createMySQLTables(connection);
            } else {
                createSQLiteTables(connection);
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao criar tabelas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createMySQLTables(Connection connection) throws SQLException {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS horus_tags (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "display_name VARCHAR(100), " +
                "prefix TEXT, " +
                "suffix TEXT, " +
                "description TEXT, " +
                "category VARCHAR(50), " +
                "rarity VARCHAR(20), " +
                "type VARCHAR(20), " +
                "price DOUBLE DEFAULT 0, " +
                "permission VARCHAR(100), " +
                "required_groups TEXT, " +
                "required_achievements TEXT, " +
                "duration BIGINT DEFAULT -1, " +
                "priority INT DEFAULT 0, " +
                "animated BOOLEAN DEFAULT FALSE, " +
                "animation_frames TEXT, " +
                "animation_speed BIGINT, " +
                "animation_type VARCHAR(20), " +
                "glow BOOLEAN DEFAULT FALSE, " +
                "particle_effect VARCHAR(50), " +
                "color VARCHAR(20), " +
                "format VARCHAR(50), " +
                "title TEXT, " +
                "subtitle TEXT, " +
                "title_animated BOOLEAN DEFAULT FALSE, " +
                "title_animation_frames TEXT, " +
                "title_animation_speed BIGINT, " +
                "subtitle_animated BOOLEAN DEFAULT FALSE, " +
                "subtitle_animation_frames TEXT, " +
                "subtitle_animation_speed BIGINT, " +
                "limited BOOLEAN DEFAULT FALSE, " +
                "max_owners INT, " +
                "seasonal BOOLEAN DEFAULT FALSE, " +
                "season VARCHAR(20), " +
                "purchasable BOOLEAN DEFAULT FALSE, " +
                "tradeable BOOLEAN DEFAULT FALSE, " +
                "giftable BOOLEAN DEFAULT FALSE, " +
                "conditions TEXT, " +
                "regions TEXT, " +
                "time_restriction VARCHAR(50), " +
                "enabled BOOLEAN DEFAULT TRUE, " +
                "created_at BIGINT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS horus_player_tags (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "tag_id VARCHAR(50) NOT NULL, " +
                "obtained_at BIGINT, " +
                "expires_at BIGINT, " +
                "active BOOLEAN DEFAULT FALSE, " +
                "favorite BOOLEAN DEFAULT FALSE, " +
                "obtained_method VARCHAR(50), " +
                "UNIQUE KEY unique_player_tag (player_uuid, tag_id), " +
                "INDEX idx_player (player_uuid), " +
                "INDEX idx_tag (tag_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS horus_achievements (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "tag_reward VARCHAR(50), " +
                "type VARCHAR(20), " +
                "required_value INT DEFAULT 1, " +
                "required_tags TEXT, " +
                "enabled BOOLEAN DEFAULT TRUE, " +
                "created_at BIGINT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS horus_player_achievements (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "achievement_id VARCHAR(50) NOT NULL, " +
                "progress INT DEFAULT 0, " +
                "completed BOOLEAN DEFAULT FALSE, " +
                "completed_at BIGINT, " +
                "UNIQUE KEY unique_player_achievement (player_uuid, achievement_id), " +
                "INDEX idx_player (player_uuid)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS horus_purchases (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "tag_id VARCHAR(50) NOT NULL, " +
                "price DOUBLE, " +
                "purchased_at BIGINT, " +
                "INDEX idx_player (player_uuid)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        };
        
        for (String table : tables) {
            try (PreparedStatement stmt = connection.prepareStatement(table)) {
                stmt.execute();
            }
        }
    }
    
    private void createSQLiteTables(Connection connection) throws SQLException {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS horus_tags (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "display_name VARCHAR(100), " +
                "prefix TEXT, " +
                "suffix TEXT, " +
                "description TEXT, " +
                "category VARCHAR(50), " +
                "rarity VARCHAR(20), " +
                "type VARCHAR(20), " +
                "price DOUBLE DEFAULT 0, " +
                "permission VARCHAR(100), " +
                "required_groups TEXT, " +
                "required_achievements TEXT, " +
                "duration BIGINT DEFAULT -1, " +
                "priority INT DEFAULT 0, " +
                "animated BOOLEAN DEFAULT FALSE, " +
                "animation_frames TEXT, " +
                "animation_speed BIGINT, " +
                "animation_type VARCHAR(20), " +
                "glow BOOLEAN DEFAULT FALSE, " +
                "particle_effect VARCHAR(50), " +
                "color VARCHAR(20), " +
                "format VARCHAR(50), " +
                "title TEXT, " +
                "subtitle TEXT, " +
                "title_animated BOOLEAN DEFAULT FALSE, " +
                "title_animation_frames TEXT, " +
                "title_animation_speed BIGINT, " +
                "subtitle_animated BOOLEAN DEFAULT FALSE, " +
                "subtitle_animation_frames TEXT, " +
                "subtitle_animation_speed BIGINT, " +
                "limited BOOLEAN DEFAULT FALSE, " +
                "max_owners INT, " +
                "seasonal BOOLEAN DEFAULT FALSE, " +
                "season VARCHAR(20), " +
                "purchasable BOOLEAN DEFAULT FALSE, " +
                "tradeable BOOLEAN DEFAULT FALSE, " +
                "giftable BOOLEAN DEFAULT FALSE, " +
                "conditions TEXT, " +
                "regions TEXT, " +
                "time_restriction VARCHAR(50), " +
                "enabled BOOLEAN DEFAULT TRUE, " +
                "created_at BIGINT" +
                ")",
            "CREATE TABLE IF NOT EXISTS horus_player_tags (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "tag_id VARCHAR(50) NOT NULL, " +
                "obtained_at BIGINT, " +
                "expires_at BIGINT, " +
                "active BOOLEAN DEFAULT FALSE, " +
                "favorite BOOLEAN DEFAULT FALSE, " +
                "obtained_method VARCHAR(50), " +
                "UNIQUE(player_uuid, tag_id)" +
                ")",
            "CREATE TABLE IF NOT EXISTS horus_achievements (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "tag_reward VARCHAR(50), " +
                "type VARCHAR(20), " +
                "required_value INT DEFAULT 1, " +
                "required_tags TEXT, " +
                "enabled BOOLEAN DEFAULT TRUE, " +
                "created_at BIGINT" +
                ")",
            "CREATE TABLE IF NOT EXISTS horus_player_achievements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "achievement_id VARCHAR(50) NOT NULL, " +
                "progress INT DEFAULT 0, " +
                "completed BOOLEAN DEFAULT FALSE, " +
                "completed_at BIGINT, " +
                "UNIQUE(player_uuid, achievement_id)" +
                ")",
            "CREATE TABLE IF NOT EXISTS horus_purchases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "tag_id VARCHAR(50) NOT NULL, " +
                "price DOUBLE, " +
                "purchased_at BIGINT" +
                ")"
        };
        
        for (String table : tables) {
            try (PreparedStatement stmt = connection.prepareStatement(table)) {
                stmt.execute();
            }
        }
    }
    
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    // Implementação dos métodos da interface (similar ao código atual do DatabaseManager)
    // Por questões de espaço, vou implementar os métodos principais
    
    @Override
    public void saveTag(Tag tag) {
        try (Connection connection = getConnection()) {
            String query;
            if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                query = "INSERT INTO horus_tags (id, name, display_name, prefix, suffix, description, category, rarity, type, price, permission, " +
                        "required_groups, required_achievements, duration, priority, animated, animation_frames, animation_speed, animation_type, glow, particle_effect, " +
                        "color, format, title, subtitle, title_animated, title_animation_frames, title_animation_speed, subtitle_animated, subtitle_animation_frames, subtitle_animation_speed, " +
                        "limited, max_owners, seasonal, season, purchasable, tradeable, giftable, conditions, regions, time_restriction, enabled, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name=?, display_name=?, prefix=?, suffix=?, description=?, category=?, rarity=?, type=?, price=?, permission=?, " +
                        "required_groups=?, required_achievements=?, duration=?, priority=?, animated=?, animation_frames=?, animation_speed=?, animation_type=?, glow=?, particle_effect=?, " +
                        "color=?, format=?, title=?, subtitle=?, title_animated=?, title_animation_frames=?, title_animation_speed=?, subtitle_animated=?, subtitle_animation_frames=?, subtitle_animation_speed=?, " +
                        "limited=?, max_owners=?, seasonal=?, season=?, purchasable=?, tradeable=?, giftable=?, conditions=?, regions=?, time_restriction=?, enabled=?";
            } else {
                query = "INSERT OR REPLACE INTO horus_tags (id, name, display_name, prefix, suffix, description, category, rarity, type, price, permission, " +
                        "required_groups, required_achievements, duration, priority, animated, animation_frames, animation_speed, animation_type, glow, particle_effect, " +
                        "color, format, title, subtitle, title_animated, title_animation_frames, title_animation_speed, subtitle_animated, subtitle_animation_frames, subtitle_animation_speed, " +
                        "limited, max_owners, seasonal, season, purchasable, tradeable, giftable, conditions, regions, time_restriction, enabled, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                int index = 1;
                stmt.setString(index++, tag.getId());
                stmt.setString(index++, tag.getName());
                stmt.setString(index++, tag.getDisplayName());
                stmt.setString(index++, tag.getPrefix());
                stmt.setString(index++, tag.getSuffix());
                stmt.setString(index++, tag.getDescription() != null ? String.join("|", tag.getDescription()) : null);
                stmt.setString(index++, tag.getCategory());
                stmt.setString(index++, tag.getRarity() != null ? tag.getRarity().name() : null);
                stmt.setString(index++, tag.getType() != null ? tag.getType().name() : null);
                stmt.setDouble(index++, tag.getPrice());
                stmt.setString(index++, tag.getPermission());
                stmt.setString(index++, tag.getRequiredGroups() != null ? String.join(",", tag.getRequiredGroups()) : null);
                stmt.setString(index++, tag.getRequiredAchievements() != null ? String.join(",", tag.getRequiredAchievements()) : null);
                stmt.setLong(index++, tag.getDuration());
                stmt.setInt(index++, tag.getPriority());
                stmt.setBoolean(index++, tag.isAnimated());
                stmt.setString(index++, tag.getAnimationFrames() != null ? String.join("|", tag.getAnimationFrames()) : null);
                stmt.setLong(index++, tag.getAnimationSpeed());
                stmt.setString(index++, tag.getAnimationType());
                stmt.setBoolean(index++, tag.isGlow());
                stmt.setString(index++, tag.getParticleEffect());
                stmt.setString(index++, tag.getColor());
                stmt.setString(index++, tag.getFormat());
                stmt.setString(index++, tag.getTitle());
                stmt.setString(index++, tag.getSubtitle());
                stmt.setBoolean(index++, tag.isTitleAnimated());
                stmt.setString(index++, tag.getTitleAnimationFrames() != null ? String.join("|", tag.getTitleAnimationFrames()) : null);
                stmt.setLong(index++, tag.getTitleAnimationSpeed());
                stmt.setBoolean(index++, tag.isSubtitleAnimated());
                stmt.setString(index++, tag.getSubtitleAnimationFrames() != null ? String.join("|", tag.getSubtitleAnimationFrames()) : null);
                stmt.setLong(index++, tag.getSubtitleAnimationSpeed());
                stmt.setBoolean(index++, tag.isLimited());
                stmt.setInt(index++, tag.getMaxOwners());
                stmt.setBoolean(index++, tag.isSeasonal());
                stmt.setString(index++, tag.getSeason());
                stmt.setBoolean(index++, tag.isPurchasable());
                stmt.setBoolean(index++, tag.isTradeable());
                stmt.setBoolean(index++, tag.isGiftable());
                stmt.setString(index++, tag.getConditions() != null ? String.join(",", tag.getConditions()) : null);
                stmt.setString(index++, tag.getRegions() != null ? String.join(",", tag.getRegions()) : null);
                stmt.setString(index++, tag.getTimeRestriction());
                stmt.setBoolean(index++, tag.isEnabled());
                stmt.setLong(index++, tag.getCreatedAt());
                
                if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                    stmt.setString(index++, tag.getName());
                    stmt.setString(index++, tag.getDisplayName());
                    stmt.setString(index++, tag.getPrefix());
                    stmt.setString(index++, tag.getSuffix());
                    stmt.setString(index++, tag.getDescription() != null ? String.join("|", tag.getDescription()) : null);
                    stmt.setString(index++, tag.getCategory());
                    stmt.setString(index++, tag.getRarity() != null ? tag.getRarity().name() : null);
                    stmt.setString(index++, tag.getType() != null ? tag.getType().name() : null);
                    stmt.setDouble(index++, tag.getPrice());
                    stmt.setString(index++, tag.getPermission());
                    stmt.setString(index++, tag.getRequiredGroups() != null ? String.join(",", tag.getRequiredGroups()) : null);
                    stmt.setString(index++, tag.getRequiredAchievements() != null ? String.join(",", tag.getRequiredAchievements()) : null);
                    stmt.setLong(index++, tag.getDuration());
                    stmt.setInt(index++, tag.getPriority());
                    stmt.setBoolean(index++, tag.isAnimated());
                    stmt.setString(index++, tag.getAnimationFrames() != null ? String.join("|", tag.getAnimationFrames()) : null);
                    stmt.setLong(index++, tag.getAnimationSpeed());
                    stmt.setString(index++, tag.getAnimationType());
                    stmt.setBoolean(index++, tag.isGlow());
                    stmt.setString(index++, tag.getParticleEffect());
                    stmt.setString(index++, tag.getColor());
                    stmt.setString(index++, tag.getFormat());
                    stmt.setString(index++, tag.getTitle());
                    stmt.setString(index++, tag.getSubtitle());
                    stmt.setBoolean(index++, tag.isTitleAnimated());
                    stmt.setString(index++, tag.getTitleAnimationFrames() != null ? String.join("|", tag.getTitleAnimationFrames()) : null);
                    stmt.setLong(index++, tag.getTitleAnimationSpeed());
                    stmt.setBoolean(index++, tag.isSubtitleAnimated());
                    stmt.setString(index++, tag.getSubtitleAnimationFrames() != null ? String.join("|", tag.getSubtitleAnimationFrames()) : null);
                    stmt.setLong(index++, tag.getSubtitleAnimationSpeed());
                    stmt.setBoolean(index++, tag.isLimited());
                    stmt.setInt(index++, tag.getMaxOwners());
                    stmt.setBoolean(index++, tag.isSeasonal());
                    stmt.setString(index++, tag.getSeason());
                    stmt.setBoolean(index++, tag.isPurchasable());
                    stmt.setBoolean(index++, tag.isTradeable());
                    stmt.setBoolean(index++, tag.isGiftable());
                    stmt.setString(index++, tag.getConditions() != null ? String.join(",", tag.getConditions()) : null);
                    stmt.setString(index++, tag.getRegions() != null ? String.join(",", tag.getRegions()) : null);
                    stmt.setString(index++, tag.getTimeRestriction());
                    stmt.setBoolean(index++, tag.isEnabled());
                }
                
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao salvar tag: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Tag getTag(String id) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_tags WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return loadTagFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar tag: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_tags";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(loadTagFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar tags: " + e.getMessage());
        }
        return tags;
    }
    
    @Override
    public void deleteTag(String id) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM horus_tags WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao deletar tag: " + e.getMessage());
        }
    }
    
    private Tag loadTagFromResultSet(ResultSet rs) throws SQLException {
        Tag tag = new Tag(rs.getString("id"), rs.getString("name"));
        tag.setDisplayName(rs.getString("display_name"));
        tag.setPrefix(rs.getString("prefix"));
        tag.setSuffix(rs.getString("suffix"));
        
        String description = rs.getString("description");
        if (description != null && !description.isEmpty()) {
            tag.setDescription(Arrays.asList(description.split("\\|")));
        }
        
        tag.setCategory(rs.getString("category"));
        
        String rarity = rs.getString("rarity");
        if (rarity != null) {
            try {
                tag.setRarity(Tag.TagRarity.valueOf(rarity));
            } catch (Exception e) {}
        }
        
        String type = rs.getString("type");
        if (type != null) {
            try {
                tag.setType(Tag.TagType.valueOf(type));
            } catch (Exception e) {}
        }
        
        tag.setPrice(rs.getDouble("price"));
        tag.setPermission(rs.getString("permission"));
        
        String requiredGroups = rs.getString("required_groups");
        if (requiredGroups != null && !requiredGroups.isEmpty()) {
            tag.setRequiredGroups(new HashSet<>(Arrays.asList(requiredGroups.split(","))));
        }
        
        String requiredAchievements = rs.getString("required_achievements");
        if (requiredAchievements != null && !requiredAchievements.isEmpty()) {
            tag.setRequiredAchievements(new HashSet<>(Arrays.asList(requiredAchievements.split(","))));
        }
        
        tag.setDuration(rs.getLong("duration"));
        tag.setPriority(rs.getInt("priority"));
        tag.setAnimated(rs.getBoolean("animated"));
        
        String animationFrames = rs.getString("animation_frames");
        if (animationFrames != null && !animationFrames.isEmpty()) {
            tag.setAnimationFrames(Arrays.asList(animationFrames.split("\\|")));
        }
        
        tag.setAnimationSpeed(rs.getLong("animation_speed"));
        tag.setAnimationType(rs.getString("animation_type"));
        tag.setGlow(rs.getBoolean("glow"));
        tag.setParticleEffect(rs.getString("particle_effect"));
        tag.setColor(rs.getString("color"));
        tag.setFormat(rs.getString("format"));
        tag.setTitle(rs.getString("title"));
        tag.setSubtitle(rs.getString("subtitle"));
        tag.setTitleAnimated(rs.getBoolean("title_animated"));
        
        String titleAnimationFrames = rs.getString("title_animation_frames");
        if (titleAnimationFrames != null && !titleAnimationFrames.isEmpty()) {
            tag.setTitleAnimationFrames(Arrays.asList(titleAnimationFrames.split("\\|")));
        }
        
        tag.setTitleAnimationSpeed(rs.getLong("title_animation_speed"));
        tag.setSubtitleAnimated(rs.getBoolean("subtitle_animated"));
        
        String subtitleAnimationFrames = rs.getString("subtitle_animation_frames");
        if (subtitleAnimationFrames != null && !subtitleAnimationFrames.isEmpty()) {
            tag.setSubtitleAnimationFrames(Arrays.asList(subtitleAnimationFrames.split("\\|")));
        }
        
        tag.setSubtitleAnimationSpeed(rs.getLong("subtitle_animation_speed"));
        tag.setLimited(rs.getBoolean("limited"));
        tag.setMaxOwners(rs.getInt("max_owners"));
        tag.setSeasonal(rs.getBoolean("seasonal"));
        tag.setSeason(rs.getString("season"));
        tag.setPurchasable(rs.getBoolean("purchasable"));
        tag.setTradeable(rs.getBoolean("tradeable"));
        tag.setGiftable(rs.getBoolean("giftable"));
        
        String conditions = rs.getString("conditions");
        if (conditions != null && !conditions.isEmpty()) {
            tag.setConditions(new HashSet<>(Arrays.asList(conditions.split(","))));
        }
        
        String regions = rs.getString("regions");
        if (regions != null && !regions.isEmpty()) {
            tag.setRegions(new HashSet<>(Arrays.asList(regions.split(","))));
        }
        
        tag.setTimeRestriction(rs.getString("time_restriction"));
        tag.setEnabled(rs.getBoolean("enabled"));
        tag.setCreatedAt(rs.getLong("created_at"));
        
        return tag;
    }
    
    // Implementações restantes dos métodos da interface (PlayerTag, Achievement, etc.)
    // Por questões de espaço, vou criar métodos stub que seguem o mesmo padrão
    
    @Override
    public void savePlayerTag(PlayerTag playerTag) {
        try (Connection connection = getConnection()) {
            String query;
            if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                query = "INSERT INTO horus_player_tags (player_uuid, tag_id, obtained_at, expires_at, active, favorite, obtained_method) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE obtained_at=?, expires_at=?, active=?, favorite=?, obtained_method=?";
            } else {
                query = "INSERT OR REPLACE INTO horus_player_tags (player_uuid, tag_id, obtained_at, expires_at, active, favorite, obtained_method) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerTag.getPlayerUUID().toString());
                stmt.setString(2, playerTag.getTagId());
                stmt.setLong(3, playerTag.getObtainedAt());
                stmt.setLong(4, playerTag.getExpiresAt());
                stmt.setBoolean(5, playerTag.isActive());
                stmt.setBoolean(6, playerTag.isFavorite());
                stmt.setString(7, playerTag.getObtainedMethod());
                
                if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                    stmt.setLong(8, playerTag.getObtainedAt());
                    stmt.setLong(9, playerTag.getExpiresAt());
                    stmt.setBoolean(10, playerTag.isActive());
                    stmt.setBoolean(11, playerTag.isFavorite());
                    stmt.setString(12, playerTag.getObtainedMethod());
                }
                
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao salvar player tag: " + e.getMessage());
        }
    }
    
    @Override
    public List<PlayerTag> getPlayerTags(UUID uuid) {
        List<PlayerTag> tags = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_player_tags WHERE player_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        PlayerTag playerTag = new PlayerTag(
                            uuid,
                            rs.getString("tag_id")
                        );
                        playerTag.setObtainedAt(rs.getLong("obtained_at"));
                        playerTag.setExpiresAt(rs.getLong("expires_at"));
                        playerTag.setActive(rs.getBoolean("active"));
                        playerTag.setFavorite(rs.getBoolean("favorite"));
                        playerTag.setObtainedMethod(rs.getString("obtained_method"));
                        tags.add(playerTag);
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar player tags: " + e.getMessage());
        }
        return tags;
    }
    
    @Override
    public PlayerTag getPlayerTag(UUID uuid, String tagId) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_player_tags WHERE player_uuid = ? AND tag_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, tagId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        PlayerTag playerTag = new PlayerTag(
                            uuid,
                            rs.getString("tag_id")
                        );
                        playerTag.setObtainedAt(rs.getLong("obtained_at"));
                        playerTag.setExpiresAt(rs.getLong("expires_at"));
                        playerTag.setActive(rs.getBoolean("active"));
                        playerTag.setFavorite(rs.getBoolean("favorite"));
                        playerTag.setObtainedMethod(rs.getString("obtained_method"));
                        return playerTag;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar player tag: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public void deletePlayerTag(UUID uuid, String tagId) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM horus_player_tags WHERE player_uuid = ? AND tag_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, tagId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao deletar player tag: " + e.getMessage());
        }
    }
    
    @Override
    public void updatePlayerTagActive(UUID uuid, String tagId, boolean active) {
        try (Connection connection = getConnection()) {
            String query = "UPDATE horus_player_tags SET active = ? WHERE player_uuid = ? AND tag_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setBoolean(1, active);
                stmt.setString(2, uuid.toString());
                stmt.setString(3, tagId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao atualizar player tag active: " + e.getMessage());
        }
    }
    
    @Override
    public void updatePlayerTagFavorite(UUID uuid, String tagId, boolean favorite) {
        try (Connection connection = getConnection()) {
            String query = "UPDATE horus_player_tags SET favorite = ? WHERE player_uuid = ? AND tag_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setBoolean(1, favorite);
                stmt.setString(2, uuid.toString());
                stmt.setString(3, tagId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao atualizar player tag favorite: " + e.getMessage());
        }
    }
    
    @Override
    public void saveAchievement(Achievement achievement) {
        try (Connection connection = getConnection()) {
            String query;
            if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                query = "INSERT INTO horus_achievements (id, name, description, tag_reward, type, required_value, required_tags, enabled, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name=?, description=?, tag_reward=?, type=?, required_value=?, required_tags=?, enabled=?";
            } else {
                query = "INSERT OR REPLACE INTO horus_achievements (id, name, description, tag_reward, type, required_value, required_tags, enabled, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, achievement.getId());
                stmt.setString(2, achievement.getName());
                stmt.setString(3, String.join("|", achievement.getDescription()));
                stmt.setString(4, achievement.getTagReward());
                stmt.setString(5, achievement.getType() != null ? achievement.getType().name() : null);
                stmt.setInt(6, achievement.getRequiredValue());
                stmt.setString(7, achievement.getRequiredTags() != null ? String.join(",", achievement.getRequiredTags()) : null);
                stmt.setBoolean(8, achievement.isEnabled());
                stmt.setLong(9, achievement.getCreatedAt());
                
                if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                    stmt.setString(10, achievement.getName());
                    stmt.setString(11, String.join("|", achievement.getDescription()));
                    stmt.setString(12, achievement.getTagReward());
                    stmt.setString(13, achievement.getType() != null ? achievement.getType().name() : null);
                    stmt.setInt(14, achievement.getRequiredValue());
                    stmt.setString(15, achievement.getRequiredTags() != null ? String.join(",", achievement.getRequiredTags()) : null);
                    stmt.setBoolean(16, achievement.isEnabled());
                }
                
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao salvar achievement: " + e.getMessage());
        }
    }
    
    @Override
    public Achievement getAchievement(String id) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_achievements WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Achievement achievement = new Achievement(id, rs.getString("name"));
                        achievement.setDescription(rs.getString("description"));
                        achievement.setTagReward(rs.getString("tag_reward"));
                        String type = rs.getString("type");
                        if (type != null) {
                            try {
                                achievement.setType(Achievement.AchievementType.valueOf(type));
                            } catch (Exception e) {}
                        }
                        achievement.setRequiredValue(rs.getInt("required_value"));
                        String requiredTags = rs.getString("required_tags");
                        if (requiredTags != null && !requiredTags.isEmpty()) {
                            achievement.setRequiredTags(new HashSet<>(Arrays.asList(requiredTags.split(","))));
                        }
                        achievement.setEnabled(rs.getBoolean("enabled"));
                        achievement.setCreatedAt(rs.getLong("created_at"));
                        return achievement;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar achievement: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Achievement> getAllAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_achievements";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Achievement achievement = new Achievement(rs.getString("id"), rs.getString("name"));
                    achievement.setDescription(rs.getString("description"));
                    achievement.setTagReward(rs.getString("tag_reward"));
                    String type = rs.getString("type");
                    if (type != null) {
                        try {
                            achievement.setType(Achievement.AchievementType.valueOf(type));
                        } catch (Exception e) {}
                    }
                    achievement.setRequiredValue(rs.getInt("required_value"));
                    String requiredTags = rs.getString("required_tags");
                    if (requiredTags != null && !requiredTags.isEmpty()) {
                        achievement.setRequiredTags(new HashSet<>(Arrays.asList(requiredTags.split(","))));
                    }
                    achievement.setEnabled(rs.getBoolean("enabled"));
                    achievement.setCreatedAt(rs.getLong("created_at"));
                    achievements.add(achievement);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar achievements: " + e.getMessage());
        }
        return achievements;
    }
    
    @Override
    public void deleteAchievement(String id) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM horus_achievements WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao deletar achievement: " + e.getMessage());
        }
    }
    
    @Override
    public void savePlayerAchievement(PlayerAchievement playerAchievement) {
        try (Connection connection = getConnection()) {
            String query;
            if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                query = "INSERT INTO horus_player_achievements (player_uuid, achievement_id, progress, completed, completed_at) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE progress=?, completed=?, completed_at=?";
            } else {
                query = "INSERT OR REPLACE INTO horus_player_achievements (player_uuid, achievement_id, progress, completed, completed_at) " +
                        "VALUES (?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerAchievement.getPlayerUUID().toString());
                stmt.setString(2, playerAchievement.getAchievementId());
                stmt.setInt(3, playerAchievement.getProgress());
                stmt.setBoolean(4, playerAchievement.isCompleted());
                stmt.setLong(5, playerAchievement.getCompletedAt());
                
                if (databaseType == DatabaseManager.DatabaseType.MYSQL) {
                    stmt.setInt(6, playerAchievement.getProgress());
                    stmt.setBoolean(7, playerAchievement.isCompleted());
                    stmt.setLong(8, playerAchievement.getCompletedAt());
                }
                
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao salvar player achievement: " + e.getMessage());
        }
    }
    
    @Override
    public PlayerAchievement getPlayerAchievement(UUID uuid, String achievementId) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_player_achievements WHERE player_uuid = ? AND achievement_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, achievementId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        PlayerAchievement playerAchievement = new PlayerAchievement(uuid, achievementId);
                        playerAchievement.setProgress(rs.getInt("progress"));
                        playerAchievement.setCompleted(rs.getBoolean("completed"));
                        playerAchievement.setCompletedAt(rs.getLong("completed_at"));
                        return playerAchievement;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar player achievement: " + e.getMessage());
        }
        return new PlayerAchievement(uuid, achievementId);
    }
    
    @Override
    public List<PlayerAchievement> getPlayerAchievements(UUID uuid) {
        List<PlayerAchievement> achievements = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_player_achievements WHERE player_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        PlayerAchievement playerAchievement = new PlayerAchievement(
                            uuid,
                            rs.getString("achievement_id")
                        );
                        playerAchievement.setProgress(rs.getInt("progress"));
                        playerAchievement.setCompleted(rs.getBoolean("completed"));
                        playerAchievement.setCompletedAt(rs.getLong("completed_at"));
                        achievements.add(playerAchievement);
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar player achievements: " + e.getMessage());
        }
        return achievements;
    }
    
    @Override
    public void savePurchase(UUID uuid, String tagId, double price, long timestamp) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO horus_purchases (player_uuid, tag_id, price, purchased_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, tagId);
                stmt.setDouble(3, price);
                stmt.setLong(4, timestamp);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao salvar purchase: " + e.getMessage());
        }
    }
    
    @Override
    public List<PurchaseRecord> getPlayerPurchases(UUID uuid) {
        List<PurchaseRecord> purchases = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM horus_purchases WHERE player_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        purchases.add(new PurchaseRecord(
                            uuid,
                            rs.getString("tag_id"),
                            rs.getDouble("price"),
                            rs.getLong("purchased_at")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.severe("Erro ao carregar purchases: " + e.getMessage());
        }
        return purchases;
    }
}

