package dev.artix.horus.commands;

import dev.artix.horus.Horus;
import dev.artix.horus.utils.LoggerUtil;
import org.bukkit.command.CommandMap;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandManager {
    
    private final Horus plugin;
    private final List<CommandArg> registeredCommands;
    private CommandMap commandMap;
    
    public CommandManager(Horus plugin) {
        this.plugin = plugin;
        this.registeredCommands = new ArrayList<>();
        this.commandMap = getCommandMap();
    }
    
    private CommandMap getCommandMap() {
        try {
            Method getCommandMapMethod = plugin.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) getCommandMapMethod.invoke(plugin.getServer());
        } catch (Exception e) {
            try {
                Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                return (CommandMap) commandMapField.get(plugin.getServer());
            } catch (Exception e2) {
                LoggerUtil.severe("Não foi possível obter o CommandMap. Comandos podem não funcionar corretamente.");
                e2.printStackTrace();
                return null;
            }
        }
    }
    
    public void registerCommand(CommandArg commandArg) {
        if (commandMap == null) {
            LoggerUtil.severe("CommandMap não disponível. Não foi possível registrar o comando: " + commandArg.getName());
            return;
        }
        
        try {
            Command command = commandArg.toBukkitCommand();
            
            if (commandMap.getCommand(commandArg.getName()) != null) {
                unregisterCommand(commandArg.getName());
            }
            
            commandMap.register(plugin.getDescription().getName().toLowerCase(), command);
            registeredCommands.add(commandArg);
            
            LoggerUtil.debug("Comando registrado: " + commandArg.getName());
        } catch (Exception e) {
            LoggerUtil.severe("Erro ao registrar comando " + commandArg.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void registerCommands(CommandArg... commandArgs) {
        for (CommandArg commandArg : commandArgs) {
            registerCommand(commandArg);
        }
    }
    
    public void unregisterCommand(String commandName) {
        if (commandMap == null) {
            return;
        }
        
        try {
            Command command = commandMap.getCommand(commandName);
            if (command != null) {
                command.unregister(commandMap);
                
                try {
                    Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                    knownCommandsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                    if (knownCommands != null) {
                        knownCommands.remove(commandName.toLowerCase());
                        for (String alias : command.getAliases()) {
                            knownCommands.remove(alias.toLowerCase());
                        }
                    }
                } catch (Exception e) {
                }
            }
            
            registeredCommands.removeIf(cmd -> cmd.getName().equals(commandName));
        } catch (Exception e) {
            LoggerUtil.severe("Erro ao desregistrar comando " + commandName + ": " + e.getMessage());
        }
    }
    
    public void unregisterAllCommands() {
        List<String> commandNames = new ArrayList<>();
        for (CommandArg commandArg : registeredCommands) {
            commandNames.add(commandArg.getName());
        }
        
        for (String commandName : commandNames) {
            unregisterCommand(commandName);
        }
        
        registeredCommands.clear();
    }
    
    public List<CommandArg> getRegisteredCommands() {
        return new ArrayList<>(registeredCommands);
    }
    
    public CommandArg getCommand(String name) {
        return registeredCommands.stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

