package dev.artix.horus.models;

import java.util.List;
import java.util.Set;

public class Achievement {
    
    private String id;
    private String name;
    private List<String> description;
    private String tagReward;
    private AchievementType type;
    private int requiredValue;
    private Set<String> requiredTags;
    private boolean enabled;
    private long createdAt;
    
    public Achievement(String id, String name) {
        this.id = id;
        this.name = name;
        this.enabled = true;
        this.createdAt = System.currentTimeMillis();
        this.requiredValue = 1;
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
    
    public List<String> getDescription() {
        return description;
    }
    
    public void setDescription(List<String> description) {
        this.description = description;
    }
    
    public String getTagReward() {
        return tagReward;
    }
    
    public void setTagReward(String tagReward) {
        this.tagReward = tagReward;
    }
    
    public AchievementType getType() {
        return type;
    }
    
    public void setType(AchievementType type) {
        this.type = type;
    }
    
    public int getRequiredValue() {
        return requiredValue;
    }
    
    public void setRequiredValue(int requiredValue) {
        this.requiredValue = requiredValue;
    }
    
    public Set<String> getRequiredTags() {
        return requiredTags;
    }
    
    public void setRequiredTags(Set<String> requiredTags) {
        this.requiredTags = requiredTags;
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
    
    public enum AchievementType {
        TAG_COUNT, PLAY_TIME, KILLS, DEATHS, WINS, LOSSES, CUSTOM
    }

    public void setDescription(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
    }
}

