package dev.artix.horus.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>");
    
    public static String translateColors(String text) {
        if (text == null) {
            return "";
        }
        
        text = ChatColor.translateAlternateColorCodes('&', text);
        
        try {
            text = translateHexColors(text);
            text = translateGradients(text);
        } catch (Exception e) {
        }
        
        return text;
    }
    
    private static String translateHexColors(String text) {
        try {
            Matcher matcher = HEX_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String hex = matcher.group();
                try {
                    Class<?> chatColorClass = Class.forName("net.md_5.bungee.api.ChatColor");
                    Object chatColor = chatColorClass.getMethod("of", String.class).invoke(null, hex);
                    String replacement = chatColor.toString();
                    matcher.appendReplacement(buffer, replacement);
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, hex);
                }
            }
            matcher.appendTail(buffer);
            
            return buffer.toString();
        } catch (Exception e) {
            return text;
        }
    }
    
    private static String translateGradients(String text) {
        try {
            Matcher matcher = GRADIENT_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String startColor = matcher.group(1);
                String endColor = matcher.group(2);
                String content = matcher.group(3);
                
                String gradient = createGradient(content, startColor, endColor);
                matcher.appendReplacement(buffer, gradient);
            }
            matcher.appendTail(buffer);
            
            return buffer.toString();
        } catch (Exception e) {
            return text;
        }
    }
    
    private static String createGradient(String text, String startColor, String endColor) {
        if (text.isEmpty()) {
            return text;
        }
        
        try {
            java.awt.Color start = java.awt.Color.decode(startColor);
            java.awt.Color end = java.awt.Color.decode(endColor);
            
            StringBuilder result = new StringBuilder();
            int length = text.length();
            
            for (int i = 0; i < length; i++) {
                double ratio = (double) i / (length - 1);
                int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * ratio);
                int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * ratio);
                int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * ratio);
                
                String hex = String.format("#%02x%02x%02x", red, green, blue);
                try {
                    Class<?> chatColorClass = Class.forName("net.md_5.bungee.api.ChatColor");
                    Object chatColor = chatColorClass.getMethod("of", String.class).invoke(null, hex);
                    result.append(chatColor.toString()).append(text.charAt(i));
                } catch (Exception e) {
                    result.append(text.charAt(i));
                }
            }
            
            return result.toString();
        } catch (Exception e) {
            return text;
        }
    }
}

