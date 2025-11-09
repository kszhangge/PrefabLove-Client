package dev.diona.southside.gui.hud.islandhud;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {
    
    private static final Map<String, Float> toggleAnimations = new HashMap<>();
    private static final Map<String, Float> toggleBgAnimations = new HashMap<>();
    private static final Map<String, Float> toggleSizeAnimations = new HashMap<>();
    
    public static float toggleBgRadius = 8f;
    public static float toggleButtonRadius = 6f;
    
    public static float toggleBallSpeed = 8.0f;
    public static float toggleBgSpeed = 8.0f;
    
    public static void drawMultipleNotifications(List<Toggle> notifications, float x, float y, float width, float height) {
        if (notifications.isEmpty()) return;
        
        float itemHeight = 42f;
        float itemSpacing = 5f;
        
        NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
        nanovg.setupAndDraw(true, vg -> {
            float currentY = y;
            for (Toggle notification : notifications) {
                drawSingleNotification(vg, nanovg, notification, x, currentY, width, itemHeight, 1.0f);
                currentY += itemHeight + itemSpacing;
            }
        });
    }
    
    private static float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }
    
    private static void drawSingleNotification(long vg, NanoVGHelper nanovg, Toggle notification, float x, float y, float width, float height, float alpha) {
        String moduleName = notification.getModuleName();
        boolean enabled = notification.isEnabled();
        
        float switchWidth = 32;
        float switchHeight = 16;
        
        String title = "Module Toggled";
        String moduleNameText = moduleName;
        String hasBeenText = " has been ";
        String statusText = enabled ? "Enabled" : "Disabled";
        String exclamation = "!";
        
        float titleSize = 12f;
        float statusSize = 10.5f;
        
        float titleWidth = nanovg.getTextWidth(vg, title, titleSize, Fonts.MEDIUM);
        float titleHeight = nanovg.getTextHeight(vg, titleSize, Fonts.MEDIUM);
        
        String statusLine = moduleNameText + hasBeenText + statusText + exclamation;
        float statusLineWidth = nanovg.getTextWidth(vg, statusLine, statusSize, Fonts.MEDIUM);
        float statusLineHeight = nanovg.getTextHeight(vg, statusSize, Fonts.MEDIUM);
        
        float textSpacing = 4f;
        float totalTextHeight = titleHeight + textSpacing + statusLineHeight;
        float textStartY = y + (height - totalTextHeight) / 2 - 5.45f;
        
        float maxTextWidth = Math.max(titleWidth, statusLineWidth);
        float textX = x + 6;
        
        float switchX = textX;
        float switchY = y + (height - switchHeight) / 2;
        
        drawModernToggleButton(vg, nanovg, switchX, switchY, switchHeight, enabled, alpha, moduleName);
        
        float titleX = switchX + switchWidth + 10;
        float titleY = textStartY + titleHeight;
        
        Color titleColor = new Color(255, 255, 255, (int)(255 * alpha));
        nanovg.drawText(vg, title, titleX, titleY, titleColor.getRGB(), titleSize, Fonts.MEDIUM);
        
        float statusY = titleY + textSpacing + statusLineHeight;
        float currentX = titleX;
        
        Color moduleNameColor = new Color(255, 255, 255, (int)(255 * alpha));
        Color hasBeenColor = new Color(160, 160, 160, (int)(255 * alpha));
        Color statusColor = enabled ? 
            new Color(76, 175, 80, (int)(255 * alpha)) : 
            new Color(244, 67, 54, (int)(255 * alpha));
        Color exclamationColor = new Color(255, 255, 255, (int)(255 * alpha));
        
        nanovg.drawText(vg, moduleNameText, currentX, statusY, moduleNameColor.getRGB(), statusSize, Fonts.MEDIUM);
        currentX += nanovg.getTextWidth(vg, moduleNameText, statusSize, Fonts.MEDIUM);
        
        nanovg.drawText(vg, hasBeenText, currentX, statusY, hasBeenColor.getRGB(), statusSize, Fonts.MEDIUM);
        currentX += nanovg.getTextWidth(vg, hasBeenText, statusSize, Fonts.MEDIUM);
        
        nanovg.drawText(vg, statusText, currentX, statusY, statusColor.getRGB(), statusSize, Fonts.MEDIUM);
        currentX += nanovg.getTextWidth(vg, statusText, statusSize, Fonts.MEDIUM);
        
        nanovg.drawText(vg, exclamation, currentX, statusY, exclamationColor.getRGB(), statusSize, Fonts.MEDIUM);
    }
    
    private static void updateToggleAnimations(String moduleKey, boolean enabled) {
        float targetSwitch = enabled ? 1.0f : 0.0f;
        float targetBg = enabled ? 1.0f : 0.0f;
        float targetSize = enabled ? 1.0f : 0.7f;
        
        float currentSwitch = toggleAnimations.getOrDefault(moduleKey, targetSwitch);
        float currentBg = toggleBgAnimations.getOrDefault(moduleKey, targetBg);
        float currentSize = toggleSizeAnimations.getOrDefault(moduleKey, targetSize);
        
        float ballAnimSpeed = toggleBallSpeed * 0.01f;
        float bgAnimSpeed = toggleBgSpeed * 0.001f;
        
        if (Math.abs(currentSwitch - targetSwitch) > 0.01f) {
            float delta = targetSwitch - currentSwitch;
            float newValue = currentSwitch + delta * ballAnimSpeed;
            toggleAnimations.put(moduleKey, newValue);
        } else {
            toggleAnimations.put(moduleKey, targetSwitch);
        }
        
        if (Math.abs(currentBg - targetBg) > 0.01f) {
            float delta = targetBg - currentBg;
            float newValue = currentBg + delta * bgAnimSpeed;
            toggleBgAnimations.put(moduleKey, newValue);
        } else {
            toggleBgAnimations.put(moduleKey, targetBg);
        }
        
        if (Math.abs(currentSize - targetSize) > 0.01f) {
            float delta = targetSize - currentSize;
            float newValue = currentSize + delta * ballAnimSpeed;
            toggleSizeAnimations.put(moduleKey, newValue);
        } else {
            toggleSizeAnimations.put(moduleKey, targetSize);
        }
    }
    
    private static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
    }
    
    private static void drawModernToggleButton(long vg, NanoVGHelper nanovg, float x, float y, float switchHeight, boolean enabled, float alpha, String moduleKey) {
        updateToggleAnimations(moduleKey, enabled);
        
        float switchProgress = toggleAnimations.getOrDefault(moduleKey, enabled ? 1.0f : 0.0f);
        float bgProgress = toggleBgAnimations.getOrDefault(moduleKey, enabled ? 1.0f : 0.0f);
        float sizeProgress = toggleSizeAnimations.getOrDefault(moduleKey, enabled ? 1.0f : 0.7f);
        
        float switchWidth = 32;
        
        Color bgColorOff = new Color(60, 60, 60, (int)(255 * alpha));
        Color bgColorOn = new Color(76, 175, 80, (int)(255 * alpha));
        
        int bgR = (int)(bgColorOff.getRed() + (bgColorOn.getRed() - bgColorOff.getRed()) * bgProgress);
        int bgG = (int)(bgColorOff.getGreen() + (bgColorOn.getGreen() - bgColorOff.getGreen()) * bgProgress);
        int bgB = (int)(bgColorOff.getBlue() + (bgColorOn.getBlue() - bgColorOff.getBlue()) * bgProgress);
        Color bgColor = new Color(bgR, bgG, bgB, (int)(255 * alpha));
        
        nanovg.drawRoundedRect(vg, x, y, switchWidth, switchHeight, bgColor.getRGB(), toggleBgRadius);
        
        float buttonSize = 12 * sizeProgress;
        float buttonX = x + 2 + (switchWidth - 4 - buttonSize) * switchProgress;
        float buttonY = y + (switchHeight - buttonSize) / 2;
        
        Color buttonColor = new Color(255, 255, 255, (int)(255 * alpha));
        nanovg.drawRoundedRect(vg, buttonX, buttonY, buttonSize, buttonSize, buttonColor.getRGB(), toggleButtonRadius);
    }
}
