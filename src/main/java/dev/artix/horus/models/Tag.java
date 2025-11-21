package dev.artix.horus.models;

import java.util.List;
import java.util.Set;

public class Tag {
    
    private String id;
    private String name;
    private String displayName;
    private String prefix;
    private String suffix;
    private List<String> description;
    private String category;
    private TagRarity rarity;
    private TagType type;
    private double price;
    private String permission;
    private Set<String> requiredGroups;
    private Set<String> requiredAchievements;
    private long duration;
    private int priority;
    private boolean animated;
    private List<String> animationFrames;
    private long animationSpeed;
    private String animationType;
    private boolean glow;
    private String particleEffect;
    private String color;
    private String format;
    private String title;
    private String subtitle;
    private boolean titleAnimated;
    private List<String> titleAnimationFrames;
    private long titleAnimationSpeed;
    private boolean subtitleAnimated;
    private List<String> subtitleAnimationFrames;
    private long subtitleAnimationSpeed;
    private boolean limited;
    private int maxOwners;
    private boolean seasonal;
    private String season;
    private boolean purchasable;
    private boolean tradeable;
    private boolean giftable;
    private Set<String> conditions;
    private Set<String> regions;
    private String timeRestriction;
    private boolean enabled;
    private long createdAt;
    
    public Tag(String id, String name) {
        this.id = id;
        this.name = name;
        this.displayName = name;
        this.enabled = true;
        this.createdAt = System.currentTimeMillis();
        this.priority = 0;
        this.price = 0.0;
        this.duration = -1;
        this.purchasable = false;
        this.tradeable = false;
        this.giftable = false;
        this.animated = false;
        this.glow = false;
        this.limited = false;
        this.seasonal = false;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public void setDescription(List<String> description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public TagRarity getRarity() {
        return rarity;
    }
    
    public void setRarity(TagRarity rarity) {
        this.rarity = rarity;
    }
    
    public TagType getType() {
        return type;
    }
    
    public void setType(TagType type) {
        this.type = type;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public Set<String> getRequiredGroups() {
        return requiredGroups;
    }
    
    public void setRequiredGroups(Set<String> requiredGroups) {
        this.requiredGroups = requiredGroups;
    }
    
    public Set<String> getRequiredAchievements() {
        return requiredAchievements;
    }
    
    public void setRequiredAchievements(Set<String> requiredAchievements) {
        this.requiredAchievements = requiredAchievements;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public boolean isAnimated() {
        return animated;
    }
    
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }
    
    public List<String> getAnimationFrames() {
        return animationFrames;
    }
    
    public void setAnimationFrames(List<String> animationFrames) {
        this.animationFrames = animationFrames;
    }
    
    public long getAnimationSpeed() {
        return animationSpeed;
    }
    
    public void setAnimationSpeed(long animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    
    public String getAnimationType() {
        return animationType;
    }
    
    public void setAnimationType(String animationType) {
        this.animationType = animationType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public boolean isTitleAnimated() {
        return titleAnimated;
    }
    
    public void setTitleAnimated(boolean titleAnimated) {
        this.titleAnimated = titleAnimated;
    }
    
    public List<String> getTitleAnimationFrames() {
        return titleAnimationFrames;
    }
    
    public void setTitleAnimationFrames(List<String> titleAnimationFrames) {
        this.titleAnimationFrames = titleAnimationFrames;
    }
    
    public long getTitleAnimationSpeed() {
        return titleAnimationSpeed;
    }
    
    public void setTitleAnimationSpeed(long titleAnimationSpeed) {
        this.titleAnimationSpeed = titleAnimationSpeed;
    }
    
    public boolean isSubtitleAnimated() {
        return subtitleAnimated;
    }
    
    public void setSubtitleAnimated(boolean subtitleAnimated) {
        this.subtitleAnimated = subtitleAnimated;
    }
    
    public List<String> getSubtitleAnimationFrames() {
        return subtitleAnimationFrames;
    }
    
    public void setSubtitleAnimationFrames(List<String> subtitleAnimationFrames) {
        this.subtitleAnimationFrames = subtitleAnimationFrames;
    }
    
    public long getSubtitleAnimationSpeed() {
        return subtitleAnimationSpeed;
    }
    
    public void setSubtitleAnimationSpeed(long subtitleAnimationSpeed) {
        this.subtitleAnimationSpeed = subtitleAnimationSpeed;
    }
    
    public boolean isGlow() {
        return glow;
    }
    
    public void setGlow(boolean glow) {
        this.glow = glow;
    }
    
    public String getParticleEffect() {
        return particleEffect;
    }
    
    public void setParticleEffect(String particleEffect) {
        this.particleEffect = particleEffect;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public boolean isLimited() {
        return limited;
    }
    
    public void setLimited(boolean limited) {
        this.limited = limited;
    }
    
    public int getMaxOwners() {
        return maxOwners;
    }
    
    public void setMaxOwners(int maxOwners) {
        this.maxOwners = maxOwners;
    }
    
    public boolean isSeasonal() {
        return seasonal;
    }
    
    public void setSeasonal(boolean seasonal) {
        this.seasonal = seasonal;
    }
    
    public String getSeason() {
        return season;
    }
    
    public void setSeason(String season) {
        this.season = season;
    }
    
    public boolean isPurchasable() {
        return purchasable;
    }
    
    public void setPurchasable(boolean purchasable) {
        this.purchasable = purchasable;
    }
    
    public boolean isTradeable() {
        return tradeable;
    }
    
    public void setTradeable(boolean tradeable) {
        this.tradeable = tradeable;
    }
    
    public boolean isGiftable() {
        return giftable;
    }
    
    public void setGiftable(boolean giftable) {
        this.giftable = giftable;
    }
    
    public Set<String> getConditions() {
        return conditions;
    }
    
    public void setConditions(Set<String> conditions) {
        this.conditions = conditions;
    }
    
    public Set<String> getRegions() {
        return regions;
    }
    
    public void setRegions(Set<String> regions) {
        this.regions = regions;
    }
    
    public String getTimeRestriction() {
        return timeRestriction;
    }
    
    public void setTimeRestriction(String timeRestriction) {
        this.timeRestriction = timeRestriction;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isExpired(long obtainedAt) {
        if (duration <= 0) return false;
        return System.currentTimeMillis() - obtainedAt > duration;
    }
    
    public enum TagRarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC
    }
    
    public enum TagType {
        NORMAL, SEASONAL, ACHIEVEMENT, PURCHASE, RANK, EVENT, LIMITED
    }
}

