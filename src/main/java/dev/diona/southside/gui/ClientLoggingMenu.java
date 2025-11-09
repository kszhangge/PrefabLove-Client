package dev.diona.southside.gui;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import cc.polyfrost.oneconfig.events.event.StartEvent;
import cc.polyfrost.oneconfig.internal.OneConfig;
import dev.diona.southside.PrefabLove;
import dev.diona.southside.util.authentication.AuthenticatedUser;
import dev.diona.southside.util.authentication.AuthenticationStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Color;

public final class ClientLoggingMenu extends GuiScreen {
    private static volatile boolean initialized = false;
    private static volatile boolean canLaunch = false;
    private String status = "Launching...";

    public ClientLoggingMenu() {
    }

    @Override
    public void initGui() {
        initialize();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();
        drawDefaultBackground();
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0, 2.0, 2.0);
        int sw = this.fontRenderer.getStringWidth(status);
        int x = (w / 2 - sw / 2) / 2;
        int y = (h / 2) / 2 - 6;
        this.fontRenderer.drawStringWithShadow(status, x, y, new Color(255,255,255).getRGB());
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (canLaunch) {
            canLaunch = false;
            PrefabLove.start();
            Minecraft.getMinecraft().displayGuiScreen(new ModdedMainMenu());
            EventManager.INSTANCE.post(new StartEvent());
            EventManager.INSTANCE.post(new InitializationEvent());
            OneConfig.INSTANCE.init();
            PrefabLove.moduleManager.toggleAllListeners();
        }
    }

    private synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        AuthenticationStatus.INSTANCE.user = new AuthenticatedUser("FreeUser");
        AuthenticationStatus.INSTANCE.magic = 1000000;
        status = "FreeUser";
        canLaunch = true;
    }
}