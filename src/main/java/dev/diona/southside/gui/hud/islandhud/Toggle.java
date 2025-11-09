package dev.diona.southside.gui.hud.islandhud;

public class Toggle {
    private final String moduleName;
    private final boolean enabled;
    private final long createTime;
    private final long duration;
    private boolean slidingOut = false;
    private long slideOutStartTime = 0;
    
    public Toggle(String moduleName, boolean enabled, long duration) {
        this.moduleName = moduleName;
        this.enabled = enabled;
        this.createTime = System.currentTimeMillis();
        this.duration = duration;
    }
    
    public void startSlideOut() {
        if (!slidingOut) {
            slidingOut = true;
            slideOutStartTime = System.currentTimeMillis();
        }
    }
    
    public boolean isSlidingOut() {
        return slidingOut;
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public long getTimestamp() {
        return createTime;
    }
}
