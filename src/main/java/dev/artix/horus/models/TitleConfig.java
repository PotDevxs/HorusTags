package dev.artix.horus.models;

import java.util.ArrayList;
import java.util.List;

public class TitleConfig {
    
    private String title;
    private String subtitle;
    private boolean titleAnimated;
    private List<String> titleAnimationFrames;
    private long titleAnimationSpeed;
    private boolean subtitleAnimated;
    private List<String> subtitleAnimationFrames;
    private long subtitleAnimationSpeed;
    private int fadeIn;
    private int stay;
    private int fadeOut;
    
    public TitleConfig() {
        this.titleAnimated = false;
        this.titleAnimationFrames = new ArrayList<>();
        this.titleAnimationSpeed = 2000L;
        this.subtitleAnimated = false;
        this.subtitleAnimationFrames = new ArrayList<>();
        this.subtitleAnimationSpeed = 2000L;
        this.fadeIn = 10;
        this.stay = 40;
        this.fadeOut = 10;
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
        this.titleAnimationFrames = titleAnimationFrames != null ? titleAnimationFrames : new ArrayList<>();
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
        this.subtitleAnimationFrames = subtitleAnimationFrames != null ? subtitleAnimationFrames : new ArrayList<>();
    }
    
    public long getSubtitleAnimationSpeed() {
        return subtitleAnimationSpeed;
    }
    
    public void setSubtitleAnimationSpeed(long subtitleAnimationSpeed) {
        this.subtitleAnimationSpeed = subtitleAnimationSpeed;
    }
    
    public int getFadeIn() {
        return fadeIn;
    }
    
    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }
    
    public int getStay() {
        return stay;
    }
    
    public void setStay(int stay) {
        this.stay = stay;
    }
    
    public int getFadeOut() {
        return fadeOut;
    }
    
    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }
}

