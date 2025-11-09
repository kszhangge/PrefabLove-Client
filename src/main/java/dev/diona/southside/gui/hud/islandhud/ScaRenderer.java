package dev.diona.southside.gui.hud.islandhud;

import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class ScaRenderer {
    
    private static float currentBarWidth = 0;
    private static float targetBarWidth = 0;
    public static float barAnimSpeed = 8.0f;
    
    public static void drawScaffoldNotification(long vg, NanoVGHelper nanovg, float x, float y, float width, float height) {
        ScaManager manager = ScaManager.getInstance();
        
        int blockCount = manager.getBlockCount();
        ItemStack currentBlock = manager.getCurrentBlock();
        double rawBps = manager.getBPS();
        double bps = Math.min(rawBps, 9.9);
        
        targetBarWidth = Math.min(blockCount / 64.0f, 1.0f);
        
        if (Math.abs(currentBarWidth - targetBarWidth) > 0.01f) {
            float delta = targetBarWidth - currentBarWidth;
            currentBarWidth += delta * (barAnimSpeed * 0.01f);
        } else {
            currentBarWidth = targetBarWidth;
        }
        
        final float iconSize = 16;
        final float iconX = x + 10;
        final float iconY = y + (height - iconSize) / 2;
        
        float bpsTextSize = 10f;
        String bpsText = String.format("%.1f BPS", bps);
        float bpsWidth = nanovg.getTextWidth(vg, bpsText, bpsTextSize, Fonts.MEDIUM);
        float bpsX = x + width - bpsWidth - 10;
        float bpsY = y + height / 2 + nanovg.getTextHeight(vg, bpsTextSize, Fonts.MEDIUM) / 2 - 2.8f;
        
        float barX = iconX + iconSize + 8;
        float barWidth = bpsX - barX - 8;
        float barHeight = 4;
        float barY = y + (height - barHeight) / 2;
        
        Color barBgColor = new Color(60, 60, 60, 255);
        nanovg.drawRoundedRect(vg, barX, barY, barWidth, barHeight, barBgColor.getRGB(), 2f);
        
        float filledWidth = barWidth * currentBarWidth;
        Color barFillColor = blockCount > 32 ? new Color(76, 175, 80, 255) : 
                             blockCount > 16 ? new Color(255, 193, 7, 255) : 
                             new Color(244, 67, 54, 255);
        if (filledWidth > 0) {
            nanovg.drawRoundedRect(vg, barX, barY, filledWidth, barHeight, barFillColor.getRGB(), 2f);
        }
        
        Color bpsColor = new Color(255, 255, 255, 255);
        nanovg.drawText(vg, bpsText, bpsX, bpsY, bpsColor.getRGB(), bpsTextSize, Fonts.MEDIUM);
        
        if (currentBlock == null) {
            nanovg.drawSvg(vg, SVGs.PREFABLOVE, iconX, iconY, iconSize, iconSize, Color.WHITE.getRGB(), 255);
        }
    }
    
    public static void drawScaffoldIcon(ItemStack currentBlock, float iconX, float iconY) {
        if (currentBlock != null) {
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableLighting();
            
            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(currentBlock, (int)iconX, (int)iconY);
            
            GlStateManager.disableLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }
}
