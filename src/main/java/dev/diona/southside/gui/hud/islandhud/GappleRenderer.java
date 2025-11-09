package dev.diona.southside.gui.hud.islandhud;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class GappleRenderer {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ItemStack GAPPLE_STACK = new ItemStack(Items.GOLDEN_APPLE);
    
    public static void drawGappleNotification(long vg, NanoVGHelper nanovg, float x, float y, float width, float height) {
        float padding = 13;
        float textX = x + padding;
        
        int eatTicks = GappleManager.getInstance().getEatTicks();
        String titleText = String.format("Eating %02d Ticks", eatTicks);
        float titleSize = 11f;
        float titleHeight = nanovg.getTextHeight(vg, titleSize, Fonts.MEDIUM);
        
        float barHeight = 4;
        float spacing = 12;
        
        float totalContentHeight = titleHeight + spacing + barHeight;
        float startY = y + (height - totalContentHeight) / 2;
        
        float titleTextY = startY + titleHeight;
        Color titleColor = new Color(255, 255, 255, 255);
        nanovg.drawText(vg, titleText, textX, titleTextY, titleColor.getRGB(), titleSize, Fonts.MEDIUM);
        
        float barX = textX;
        float barY = titleTextY + spacing;
        float barWidth = width - padding * 2;
        
        float progress = Math.min(eatTicks, 32) / 32f;
        
        nanovg.drawRoundedRect(vg, barX, barY, barWidth, barHeight, new Color(60, 60, 60, 255).getRGB(), 2);
        
        if (progress > 0) {
            Color progressColor = new Color(100, 200, 255, 255);
            nanovg.drawRoundedRect(vg, barX, barY, barWidth * progress, barHeight, progressColor.getRGB(), 2);
        }
    }
    
    public static void drawGappleIcon(float x, float y, float width, float height) {
        int gappleCount = GappleManager.getInstance().getGappleCount();
        
        float padding = 14;
        float textX = x + padding;
        
        int eatTicks = GappleManager.getInstance().getEatTicks();
        String titleText = String.format("Eating %02d Ticks", eatTicks);
        float titleSize = 11f;
        
        NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
        
        final float[] titleWidth = {0};
        final float[] titleHeight = {0};
        nanovg.setupAndDraw(false, vg -> {
            titleWidth[0] = nanovg.getTextWidth(vg, titleText, titleSize, Fonts.MEDIUM);
            titleHeight[0] = nanovg.getTextHeight(vg, titleSize, Fonts.MEDIUM);
        });
        
        float barHeight = 4;
        float spacing = 6;
        
        float totalContentHeight = titleHeight[0] + spacing + barHeight;
        float startY = y + (height - totalContentHeight) / 2;
        
        float titleTextY = startY + titleHeight[0];
        
        float iconSize = 12;
        float iconX = textX + titleWidth[0] + 5;
        float iconY = titleTextY - iconSize + 2;
        
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableLighting();
        
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        mc.getRenderItem().renderItemAndEffectIntoGUI(GAPPLE_STACK, (int)(iconX / 0.75f), (int)(iconY / 0.75f));
        GlStateManager.scale(1.0f / 0.75f, 1.0f / 0.75f, 1.0f / 0.75f);
        
        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        
        nanovg.setupAndDraw(true, vg -> {
            float countX = iconX + iconSize + 2;
            float countY = titleTextY - 2;
            String countText = String.format("%02d Gapple", gappleCount);
            Color countColor = new Color(255, 215, 0, 255);
            nanovg.drawText(vg, countText, countX, countY, countColor.getRGB(), 10f, Fonts.REGULAR);
        });
    }
}
