package dev.diona.southside.gui.hud.islandhud;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;

import java.awt.*;

public class BlinkRenderer {
    private static final SVG BLINK_SVG = new SVG("/assets/minecraft/southside/blink.svg");
    
    public static void drawBlinkNotification(long vg, NanoVGHelper nanovg, float x, float y, float width, float height) {
        float displaySize = 24;
        float renderSize = displaySize * 2;
        float iconX = x + 10;
        float iconY = y + (height - displaySize) / 2;

        org.lwjgl.nanovg.NanoVG.nvgSave(vg);
        org.lwjgl.nanovg.NanoVG.nvgTranslate(vg, iconX, iconY);
        org.lwjgl.nanovg.NanoVG.nvgScale(vg, displaySize / renderSize, displaySize / renderSize);
        nanovg.drawSvg(vg, BLINK_SVG, 0, 0, renderSize, renderSize, Color.WHITE.getRGB(), 255);
        org.lwjgl.nanovg.NanoVG.nvgRestore(vg);
        
        float textX = iconX + displaySize + 8;
        
        String titleText = "Packet Blinking";
        float titleSize = 11f;
        float titleHeight = nanovg.getTextHeight(vg, titleSize, Fonts.MEDIUM);
        
        int blinkTicks = BlinkManager.getInstance().getBlinkTicks();
        boolean slowRelease = BlinkManager.getInstance().isSlowRelease();
        String ticksText;
        if (slowRelease) {
            int slowReleaseTick = BlinkManager.getInstance().getSlowReleaseTick();
            ticksText = String.format("%02d/%02d Ticks", blinkTicks, slowReleaseTick);
        } else {
            ticksText = String.format("%02d Ticks", blinkTicks);
        }
        float ticksSize = 10f;
        float ticksHeight = nanovg.getTextHeight(vg, ticksSize, Fonts.REGULAR);
        
        float totalTextHeight = titleHeight + ticksHeight + 2;
        float startY = y + (height - totalTextHeight) / 2;
        
        float titleTextY = startY + titleHeight;
        Color titleColor = new Color(255, 255, 255, 255);
        nanovg.drawText(vg, titleText, textX, titleTextY, titleColor.getRGB(), titleSize, Fonts.MEDIUM);
        
        float ticksTextY = titleTextY + ticksHeight + 2;
        Color ticksColor = new Color(200, 200, 200, 255);
        nanovg.drawText(vg, ticksText, textX, ticksTextY, ticksColor.getRGB(), ticksSize, Fonts.REGULAR);
    }
}
