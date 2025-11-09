package dev.diona.southside.gui.hud.islandhud;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.ArrayList;

public class ToggleManager {
    private static ToggleManager instance;
    private final List<Toggle> notifications = new CopyOnWriteArrayList<>();
    private final int maxNotifications = 5;
    
    private ToggleManager() {}
    
    public static ToggleManager getInstance() {
        if (instance == null) {
            instance = new ToggleManager();
        }
        return instance;
    }
    
    public void addModuleNotification(String moduleName, boolean enabled) {
        for (int i = 0; i < notifications.size(); i++) {
            Toggle existing = notifications.get(i);
            if (existing.getModuleName().equals(moduleName)) {
                notifications.remove(i);
                break;
            }
        }
        
        Toggle notification = new Toggle(moduleName, enabled, 800);
        notifications.add(0, notification);
        
        if (notifications.size() > maxNotifications) {
            notifications.remove(notifications.size() - 1);
        }
    }
    
    public List<Toggle> getActiveNotifications() {
        List<Toggle> activeNotifications = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long slideOutDuration = 300;
        
        for (int i = notifications.size() - 1; i >= 0; i--) {
            Toggle notification = notifications.get(i);
            long elapsed = currentTime - notification.getTimestamp();
            
            if (elapsed < notification.getDuration() - slideOutDuration) {
                activeNotifications.add(notification);
            } else if (elapsed < notification.getDuration()) {
                if (!notification.isSlidingOut()) {
                    notification.startSlideOut();
                }
                activeNotifications.add(notification);
            } else {
                notifications.remove(i);
            }
        }
        
        return activeNotifications;
    }
}
