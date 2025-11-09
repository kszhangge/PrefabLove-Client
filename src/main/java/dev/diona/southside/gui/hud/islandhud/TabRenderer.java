package dev.diona.southside.gui.hud.islandhud;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.diona.southside.PrefabLove;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.awt.*;
import java.util.List;

public class TabRenderer {
    
    public static void drawTabList(long vg, NanoVGHelper nanovg, float x, float y, float width, float height) {
        List<NetworkPlayerInfo> players = TabManager.getInstance().getPlayers();
        
        float padding = 8;
        float titleSize = 11f;
        float lineHeight = 20f;
        float separatorHeight = 1f;
        float spacing = 12f;
        
        float currentY = y + padding;
        
        String title = "Tab List";
        float titleWidth = nanovg.getTextWidth(vg, title, titleSize, Fonts.MEDIUM);
        float titleX = x + (width - titleWidth) / 2;
        nanovg.drawText(vg, title, titleX, currentY + titleSize, Color.WHITE.getRGB(), titleSize, Fonts.MEDIUM);
        currentY += titleSize + spacing;
        
        nanovg.drawRoundedRect(vg, x + padding, currentY, width - padding * 2, separatorHeight, new Color(100, 100, 100).getRGB(), 0.5f);
        currentY += separatorHeight + spacing;
        
        if (players.isEmpty()) {
            String emptyText = "No Players";
            float emptyWidth = nanovg.getTextWidth(vg, emptyText, 9f, Fonts.REGULAR);
            float emptyX = x + (width - emptyWidth) / 2;
            nanovg.drawText(vg, emptyText, emptyX, currentY + 5.5f, new Color(150, 150, 150).getRGB(), 9f, Fonts.REGULAR);
            currentY += 9f + spacing;
        } else {
            for (int i = 0; i < players.size(); i++) {
                NetworkPlayerInfo player = players.get(i);
                String playerName = player.getGameProfile().getName();
                String displayName = player.getDisplayName() != null ? 
                    player.getDisplayName().getFormattedText() : playerName;
                
                displayName = parseColorCodes(displayName);
                
                int ping = player.getResponseTime();
                Color pingColor = getPingColor(ping);
                
                nanovg.drawText(vg, displayName, x + padding, currentY + 5f, Color.WHITE.getRGB(), 9f, Fonts.REGULAR);
                
                String pingText = String.format("%04d", ping);
                float pingWidth = nanovg.getTextWidth(vg, pingText, 8f, Fonts.REGULAR);
                nanovg.drawText(vg, pingText, x + width - padding - pingWidth, currentY + 9f, pingColor.getRGB(), 8f, Fonts.REGULAR);
                
                currentY += lineHeight;
            }
        }
        
        nanovg.drawRoundedRect(vg, x + padding, currentY, width - padding * 2, separatorHeight, new Color(100, 100, 100).getRGB(), 0.5f);
        currentY += separatorHeight + spacing;
        
        String clientName = PrefabLove.CLIENT_NAME;
        float clientNameWidth = nanovg.getTextWidth(vg, clientName, 10f, Fonts.MEDIUM);
        float clientNameX = x + (width - clientNameWidth) / 2;
        nanovg.drawText(vg, clientName, clientNameX, currentY + 10f, new Color(100, 150, 255).getRGB(), 10f, Fonts.MEDIUM);
    }
    
    private static String parseColorCodes(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
    
    private static Color getPingColor(int ping) {
        if (ping < 50) return new Color(0, 255, 0);
        if (ping < 100) return new Color(255, 255, 0);
        if (ping < 200) return new Color(255, 165, 0);
        return new Color(255, 0, 0);
    }
}
