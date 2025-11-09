package dev.diona.southside.gui.hud.islandhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TabManager {
    private static TabManager instance;
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public static TabManager getInstance() {
        if (instance == null) {
            instance = new TabManager();
        }
        return instance;
    }
    
    public boolean isTabActive() {
        return mc.gameSettings.keyBindPlayerList.isKeyDown() && mc.player != null;
    }
    
    public List<NetworkPlayerInfo> getPlayers() {
        if (mc.player == null || mc.player.connection == null) {
            return new ArrayList<>();
        }
        
        List<NetworkPlayerInfo> players = new ArrayList<>(mc.player.connection.getPlayerInfoMap());
        players.sort(Comparator.comparing(p -> p.getGameProfile().getName().toLowerCase()));
        
        return players;
    }

    public double calculateTabHeight() {
        List<NetworkPlayerInfo> players = getPlayers();

        float padding = 8;
        float titleSize = 11f;
        float lineHeight = 20f;
        float separatorHeight = 1f;
        float spacing = 12f;
        float clientNameHeight = 10f;

        double height = padding;
        height += titleSize + spacing;
        height += separatorHeight + spacing;

        if (players.isEmpty()) {
            height += 9f + spacing;
        } else {
            height += players.size() * lineHeight;
        }

        height += separatorHeight + spacing;
        height += clientNameHeight + spacing;
        height += padding;

        return height;
    }
}
