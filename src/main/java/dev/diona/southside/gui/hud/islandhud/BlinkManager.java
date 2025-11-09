package dev.diona.southside.gui.hud.islandhud;

import dev.diona.southside.PrefabLove;
import dev.diona.southside.module.modules.player.Blink;

public class BlinkManager {
    private static BlinkManager instance;
    
    public static BlinkManager getInstance() {
        if (instance == null) {
            instance = new BlinkManager();
        }
        return instance;
    }
    
    public boolean isBlinkActive() {
        return Blink.isInstanceEnabled();
    }
    
    public int getBlinkTicks() {
        Blink blink = (Blink) PrefabLove.moduleManager.getModuleByClass(Blink.class);
        if (blink != null && blink.isEnabled()) {
            return blink.packets.size();
        }
        return 0;
    }
    
    public boolean isSlowRelease() {
        Blink blink = (Blink) PrefabLove.moduleManager.getModuleByClass(Blink.class);
        if (blink != null) {
            return blink.slowReleaseValue.getValue();
        }
        return false;
    }
    
    public int getSlowReleaseTick() {
        Blink blink = (Blink) PrefabLove.moduleManager.getModuleByClass(Blink.class);
        if (blink != null) {
            return blink.slowReleaseTickValue.getValue().intValue();
        }
        return 10;
    }
}
