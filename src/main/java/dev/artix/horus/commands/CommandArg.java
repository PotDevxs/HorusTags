package dev.artix.horus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArg {
    
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;
    private final String permission;
    private final CommandExecutor executor;
    private final TabCompleter tabCompleter;
    
    private CommandArg(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.usage = builder.usage;
        this.aliases = builder.aliases;
        this.permission = builder.permission;
        this.executor = builder.executor;
        this.tabCompleter = builder.tabCompleter;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUsage() {
        return usage;
    }
    
    public List<String> getAliases() {
        return aliases;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public CommandExecutor getExecutor() {
        return executor;
    }
    
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }
    
    public Command toBukkitCommand() {
        Command command = new Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                if (executor != null) {
                    return executor.onCommand(sender, this, commandLabel, args);
                }
                return false;
            }
            
            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                if (tabCompleter != null) {
                    return tabCompleter.onTabComplete(sender, this, alias, args);
                }
                return super.tabComplete(sender, alias, args);
            }
        };
        
        if (description != null) {
            command.setDescription(description);
        }
        if (usage != null) {
            command.setUsage(usage);
        }
        if (aliases != null && !aliases.isEmpty()) {
            command.setAliases(aliases);
        }
        if (permission != null) {
            command.setPermission(permission);
        }
        
        return command;
    }
    
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    public static class Builder {
        private final String name;
        private String description;
        private String usage;
        private List<String> aliases = new ArrayList<>();
        private String permission;
        private CommandExecutor executor;
        private TabCompleter tabCompleter;
        
        public Builder(String name) {
            this.name = name;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }
        
        public Builder aliases(String... aliases) {
            this.aliases = Arrays.asList(aliases);
            return this;
        }
        
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }
        
        public Builder executor(CommandExecutor executor) {
            this.executor = executor;
            return this;
        }
        
        public Builder tabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
            return this;
        }
        
        public CommandArg build() {
            return new CommandArg(this);
        }
    }
}

