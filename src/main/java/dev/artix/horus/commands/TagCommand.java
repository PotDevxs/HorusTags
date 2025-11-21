package dev.artix.horus.commands;

import dev.artix.horus.Horus;
import dev.artix.horus.models.Tag;
import dev.artix.horus.utils.ColorUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagCommand implements CommandExecutor, TabCompleter {
    
    private final Horus plugin;
    
    public TagCommand(Horus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String selectionMode = plugin.getConfigManager().getConfig().getString("tag-selection-mode", "menu").toLowerCase();
                
                if (selectionMode.equals("chat")) {
                    plugin.getChatSelectionManager().startTagSelection(player);
                } else {
                    plugin.getGuiManager().openMainGUI(player);
                }
            } else {
                sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "set":
            case "equip":
                return handleSet(sender, args);
            case "remove":
            case "unequip":
                return handleRemove(sender);
            case "list":
                return handleList(sender);
            case "preview":
                return handlePreview(sender, args);
            case "buy":
            case "purchase":
                return handleBuy(sender, args);
            case "favorite":
            case "fav":
                return handleFavorite(sender, args);
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "edit":
                return handleEdit(sender, args);
            case "give":
                return handleGive(sender, args);
            case "reload":
                return handleReload(sender);
            case "trade":
                return handleTrade(sender, args);
            case "gift":
                return handleGift(sender, args);
            case "backup":
                return handleBackup(sender, args);
            case "restore":
                return handleRestore(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "daily":
                return handleDaily(sender);
            case "collection":
            case "collections":
                return handleCollection(sender);
            case "refund":
                return handleRefund(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        String selectionMode = plugin.getConfigManager().getConfig().getString("tag-selection-mode", "menu").toLowerCase();
        
        if (args.length < 2) {
            if (selectionMode.equals("chat")) {
                plugin.getChatSelectionManager().startTagSelection(player);
            } else {
                sender.sendMessage(ColorUtil.translateColors("&cUso: /tag set <tag>"));
            }
            return true;
        }
        
        String tagId = args[1];
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (tag == null) {
            sender.sendMessage(ColorUtil.translateColors("&cTag não encontrada."));
            return true;
        }
        
        if (!plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            return true;
        }
        
        if (!plugin.getPermissionManager().canUseTag(player, tag)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão para usar esta tag."));
            return true;
        }
        
        plugin.getPlayerTagManager().setActiveTag(player.getUniqueId(), tagId);
        sender.sendMessage(ColorUtil.translateColors("&aTag equipada: &f" + tag.getDisplayName()));
        
        if (plugin.getChatSelectionManager().hasActiveSession(player.getUniqueId())) {
            plugin.getChatSelectionManager().cancelSelection(player);
        }
        
        return true;
    }
    
    private boolean handleRemove(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getPlayerTagManager().removeActiveTag(player.getUniqueId());
        sender.sendMessage(ColorUtil.translateColors("&aTag removida."));
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        String selectionMode = plugin.getConfigManager().getConfig().getString("tag-selection-mode", "menu").toLowerCase();
        
        if (selectionMode.equals("chat")) {
            plugin.getChatSelectionManager().startTagSelection(player);
        } else {
            List<dev.artix.horus.models.PlayerTag> tags = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId());
            
            if (tags.isEmpty()) {
                sender.sendMessage(ColorUtil.translateColors("&cVocê não possui nenhuma tag."));
                return true;
            }
            
            sender.sendMessage(ColorUtil.translateColors("&6Suas tags:"));
            for (dev.artix.horus.models.PlayerTag playerTag : tags) {
                Tag tag = plugin.getTagManager().getTag(playerTag.getTagId());
                if (tag != null) {
                    String status = playerTag.isActive() ? "&a[ATIVA]" : "&7";
                    sender.sendMessage(ColorUtil.translateColors(status + " " + tag.getDisplayName()));
                }
            }
        }
        return true;
    }
    
    private boolean handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag preview <tag>"));
            return true;
        }
        
        Player player = (Player) sender;
        String tagId = args[1];
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (tag == null) {
            sender.sendMessage(ColorUtil.translateColors("&cTag não encontrada."));
            return true;
        }
        
        plugin.getGuiManager().openPreviewGUI(player, tagId);
        return true;
    }
    
    private boolean handleBuy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        String selectionMode = plugin.getConfigManager().getConfig().getString("tag-selection-mode", "menu").toLowerCase();
        
        if (args.length < 2) {
            if (selectionMode.equals("chat")) {
                plugin.getChatSelectionManager().startPurchaseSelection(player);
            } else {
                sender.sendMessage(ColorUtil.translateColors("&cUso: /tag buy <tag>"));
            }
            return true;
        }
        
        String tagId = args[1];
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (tag == null) {
            sender.sendMessage(ColorUtil.translateColors("&cTag não encontrada."));
            return true;
        }
        
        if (!tag.isPurchasable()) {
            sender.sendMessage(ColorUtil.translateColors("&cEsta tag não está disponível para compra."));
            return true;
        }
        
        if (plugin.getEconomyManager().purchaseTag(player, tag)) {
            sender.sendMessage(ColorUtil.translateColors("&aTag comprada com sucesso!"));
        } else {
            sender.sendMessage(ColorUtil.translateColors("&cErro ao comprar tag. Verifique seu saldo."));
        }
        return true;
    }
    
    private boolean handleFavorite(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag favorite <tag>"));
            return true;
        }
        
        Player player = (Player) sender;
        String tagId = args[1];
        
        if (!plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            return true;
        }
        
        boolean isFavorite = plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId()).stream()
                .anyMatch(pt -> pt.getTagId().equals(tagId) && pt.isFavorite());
        
        plugin.getPlayerTagManager().setFavorite(player.getUniqueId(), tagId, !isFavorite);
        sender.sendMessage(ColorUtil.translateColors(!isFavorite ? "&aTag adicionada aos favoritos." : "&cTag removida dos favoritos."));
        return true;
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag create <id> <nome>"));
            return true;
        }
        
        String id = args[1];
        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        Tag tag = new Tag(id, name);
        plugin.getTagManager().saveTag(tag);
        sender.sendMessage(ColorUtil.translateColors("&aTag criada: &f" + name));
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag delete <id>"));
            return true;
        }
        
        String id = args[1];
        plugin.getTagManager().deleteTag(id);
        sender.sendMessage(ColorUtil.translateColors("&aTag deletada: &f" + id));
        return true;
    }
    
    private boolean handleEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag edit <id> <propriedade> <valor>"));
            sender.sendMessage(ColorUtil.translateColors("&7Propriedades: prefix, suffix, price, permission, duration, priority"));
            return true;
        }
        
        String tagId = args[1];
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (tag == null) {
            sender.sendMessage(ColorUtil.translateColors("&cTag não encontrada."));
            return true;
        }
        
        String property = args[2].toLowerCase();
        String value = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : null;
        
        switch (property) {
            case "prefix":
                tag.setPrefix(value);
                sender.sendMessage(ColorUtil.translateColors("&aPrefixo atualizado para: &f" + value));
                break;
            case "suffix":
                tag.setSuffix(value);
                sender.sendMessage(ColorUtil.translateColors("&aSufixo atualizado para: &f" + value));
                break;
            case "price":
                try {
                    double price = Double.parseDouble(value);
                    tag.setPrice(price);
                    sender.sendMessage(ColorUtil.translateColors("&aPreço atualizado para: &f$" + price));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.translateColors("&cPreço inválido."));
                    return true;
                }
                break;
            case "permission":
                tag.setPermission(value);
                sender.sendMessage(ColorUtil.translateColors("&aPermissão atualizada para: &f" + value));
                break;
            case "duration":
                try {
                    long duration = Long.parseLong(value);
                    tag.setDuration(duration);
                    sender.sendMessage(ColorUtil.translateColors("&aDuração atualizada para: &f" + duration + "ms"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.translateColors("&cDuração inválida."));
                    return true;
                }
                break;
            case "priority":
                try {
                    int priority = Integer.parseInt(value);
                    tag.setPriority(priority);
                    sender.sendMessage(ColorUtil.translateColors("&aPrioridade atualizada para: &f" + priority));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.translateColors("&cPrioridade inválida."));
                    return true;
                }
                break;
            default:
                sender.sendMessage(ColorUtil.translateColors("&cPropriedade desconhecida: &f" + property));
                return true;
        }
        
        plugin.getTagManager().saveTag(tag);
        if (sender instanceof Player) {
            plugin.getLogManager().logTagCreate(((Player) sender).getUniqueId(), tagId);
        }
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag give <jogador> <tag>"));
            return true;
        }
        
        Player target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.translateColors("&cJogador não encontrado."));
            return true;
        }
        
        String tagId = args[2];
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (tag == null) {
            sender.sendMessage(ColorUtil.translateColors("&cTag não encontrada."));
            return true;
        }
        
        plugin.getPlayerTagManager().giveTag(target.getUniqueId(), tagId, "ADMIN");
        sender.sendMessage(ColorUtil.translateColors("&aTag dada para &f" + target.getName()));
        target.sendMessage(ColorUtil.translateColors("&aVocê recebeu a tag: &f" + tag.getDisplayName()));
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        plugin.getConfigManager().reloadConfigs();
        plugin.getTagManager().loadTags();
        plugin.getAchievementManager().loadAchievements();
        plugin.getTitleManager().reloadTitleConfigs();
        sender.sendMessage(ColorUtil.translateColors("&aPlugin recarregado!"));
        return true;
    }
    
    private boolean handleTrade(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 2 && args[1].equalsIgnoreCase("accept")) {
            return plugin.getTradeManager().acceptTrade(player);
        }
        
        if (args.length == 2 && args[1].equalsIgnoreCase("deny")) {
            return plugin.getTradeManager().denyTrade(player);
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag trade <jogador> <sua-tag> [tag-do-jogador]"));
            return true;
        }
        
        Player target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.translateColors("&cJogador não encontrado."));
            return true;
        }
        
        String senderTagId = args[2];
        String targetTagId = args.length > 3 ? args[3] : null;
        
        return plugin.getTradeManager().initiateTrade(player, target, senderTagId, targetTagId);
    }
    
    private boolean handleGift(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag gift <jogador> <tag>"));
            return true;
        }
        
        Player player = (Player) sender;
        Player target = sender.getServer().getPlayer(args[1]);
        
        if (target == null) {
            sender.sendMessage(ColorUtil.translateColors("&cJogador não encontrado."));
            return true;
        }
        
        String tagId = args[2];
        return plugin.getGiftManager().sendGift(player, target, tagId);
    }
    
    private boolean handleBackup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        if (plugin.getBackupManager().createBackup()) {
            sender.sendMessage(ColorUtil.translateColors("&aBackup criado com sucesso!"));
        } else {
            sender.sendMessage(ColorUtil.translateColors("&cErro ao criar backup."));
        }
        return true;
    }
    
    private boolean handleRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não tem permissão."));
            return true;
        }
        
        if (args.length < 2) {
            java.util.List<java.io.File> backups = plugin.getBackupManager().listBackups();
            sender.sendMessage(ColorUtil.translateColors("&6Backups disponíveis:"));
            for (int i = 0; i < Math.min(10, backups.size()); i++) {
                java.io.File backup = backups.get(i);
                sender.sendMessage(ColorUtil.translateColors("&7" + (i + 1) + ". &f" + backup.getName()));
            }
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag restore <nome-do-backup>"));
            return true;
        }
        
        String backupName = args[1];
        java.io.File backupFile = new java.io.File(plugin.getDataFolder(), "backups" + java.io.File.separator + backupName);
        
        if (plugin.getBackupManager().restoreBackup(backupFile)) {
            sender.sendMessage(ColorUtil.translateColors("&aBackup restaurado com sucesso!"));
        } else {
            sender.sendMessage(ColorUtil.translateColors("&cErro ao restaurar backup."));
        }
        return true;
    }
    
    private boolean handleStats(CommandSender sender, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("global") && sender.hasPermission("horus.admin")) {
            java.util.Map<String, Object> stats = plugin.getStatsManager().getGlobalStats();
            sender.sendMessage(ColorUtil.translateColors("&6=== Estatísticas Globais ==="));
            sender.sendMessage(ColorUtil.translateColors("&7Total de Tags: &f" + stats.get("total-tags")));
            sender.sendMessage(ColorUtil.translateColors("&7Total de Jogadores: &f" + stats.get("total-players")));
            sender.sendMessage(ColorUtil.translateColors("&7Total de Compras: &f" + stats.get("total-purchases")));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        java.util.Map<String, Object> stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
        
        sender.sendMessage(ColorUtil.translateColors("&6=== Suas Estatísticas ==="));
        sender.sendMessage(ColorUtil.translateColors("&7Total de Tags: &f" + stats.get("total-tags")));
        sender.sendMessage(ColorUtil.translateColors("&7Tags Favoritas: &f" + stats.get("favorite-tags")));
        sender.sendMessage(ColorUtil.translateColors("&7Tag Ativa: &f" + stats.get("active-tag")));
        sender.sendMessage(ColorUtil.translateColors("&7Total de Compras: &f" + stats.get("total-purchases")));
        
        return true;
    }
    
    private boolean handleDaily(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getRandomTagManager().giveRandomDailyTag(player, "default");
        return true;
    }
    
    private boolean handleCollection(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        java.util.List<dev.artix.horus.managers.CollectionManager.Collection> collections = 
            plugin.getCollectionManager().getPlayerCollections(player.getUniqueId());
        
        if (collections.isEmpty()) {
            sender.sendMessage(ColorUtil.translateColors("&cNenhuma coleção disponível."));
            return true;
        }
        
        sender.sendMessage(ColorUtil.translateColors("&6=== Suas Coleções ==="));
        for (dev.artix.horus.managers.CollectionManager.Collection collection : collections) {
            dev.artix.horus.managers.CollectionManager.CollectionProgress progress = 
                plugin.getCollectionManager().getCollectionProgress(player.getUniqueId(), collection);
            
            String status = progress.isCompleted() ? "&a[COMPLETA]" : "&7";
            sender.sendMessage(ColorUtil.translateColors(status + " &f" + collection.getName() + 
                " &7(" + progress.getCollected() + "/" + progress.getTotal() + ")"));
        }
        
        return true;
    }
    
    private boolean handleRefund(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColors("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.translateColors("&cUso: /tag refund <tag>"));
            return true;
        }
        
        Player player = (Player) sender;
        String tagId = args[1];
        Tag tag = plugin.getTagManager().getTag(tagId);
        
        if (tag == null) {
            sender.sendMessage(ColorUtil.translateColors("&cTag não encontrada."));
            return true;
        }
        
        if (!plugin.getPlayerTagManager().hasTag(player.getUniqueId(), tagId)) {
            sender.sendMessage(ColorUtil.translateColors("&cVocê não possui esta tag."));
            return true;
        }
        
        if (!plugin.getEconomyManager().canRefund(player, tag)) {
            sender.sendMessage(ColorUtil.translateColors("&cEsta tag não pode ser reembolsada. O prazo de reembolso expirou ou os reembolsos estão desabilitados."));
            return true;
        }
        
        if (plugin.getEconomyManager().refundTag(player, tag)) {
            sender.sendMessage(ColorUtil.translateColors("&aTag reembolsada com sucesso! Você recebeu $" + tag.getPrice()));
        } else {
            sender.sendMessage(ColorUtil.translateColors("&cErro ao processar reembolso."));
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.translateColors("&6=== Horus Tags ==="));
        sender.sendMessage(ColorUtil.translateColors("&e/tag - Abre o menu de tags"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag set <tag> - Equipa uma tag"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag remove - Remove a tag ativa"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag list - Lista suas tags"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag preview <tag> - Visualiza uma tag"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag buy <tag> - Compra uma tag"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag refund <tag> - Reembolsa uma tag"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag favorite <tag> - Adiciona/remove dos favoritos"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag trade <jogador> <tag> [tag-jogador] - Troca tags"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag gift <jogador> <tag> - Presenteia uma tag"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag daily - Recebe uma tag aleatória diária"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag collection - Ver suas coleções"));
        sender.sendMessage(ColorUtil.translateColors("&e/tag stats - Ver suas estatísticas"));
        
        if (sender.hasPermission("horus.admin")) {
            sender.sendMessage(ColorUtil.translateColors("&c/tag create <id> <nome> - Cria uma tag"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag edit <id> <prop> <valor> - Edita uma tag"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag delete <id> - Deleta uma tag"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag give <jogador> <tag> - Dá uma tag"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag backup - Cria um backup"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag restore <backup> - Restaura um backup"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag stats global - Estatísticas globais"));
            sender.sendMessage(ColorUtil.translateColors("&c/tag reload - Recarrega o plugin"));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "remove", "list", "preview", "buy", "favorite", "trade", "gift", "daily", "collection", "stats"));
            if (sender.hasPermission("horus.admin")) {
                completions.addAll(Arrays.asList("create", "delete", "edit", "give", "backup", "restore", "reload"));
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("set") || subCommand.equals("preview") || subCommand.equals("buy") || 
                subCommand.equals("favorite") || subCommand.equals("delete") || subCommand.equals("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (subCommand.equals("set") || subCommand.equals("favorite")) {
                        completions.addAll(plugin.getPlayerTagManager().getPlayerTags(player.getUniqueId()).stream()
                                .map(pt -> plugin.getTagManager().getTag(pt.getTagId()))
                                .filter(tag -> tag != null)
                                .map(Tag::getId)
                                .collect(Collectors.toList()));
                    } else {
                        completions.addAll(plugin.getTagManager().getAllTags().stream()
                                .map(Tag::getId)
                                .collect(Collectors.toList()));
                    }
                } else {
                    completions.addAll(plugin.getTagManager().getAllTags().stream()
                            .map(Tag::getId)
                            .collect(Collectors.toList()));
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        }
        
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}

