package dev.diona.southside;

import com.rebane2001.livemessage.Livemessage;
import dev.diona.southside.managers.*;
import dev.diona.southside.util.player.MovementUtils;
import dev.diona.southside.util.player.RotationUtil;
import dev.diona.southside.util.render.glyph.GlyphFontManager;
import me.bush.eventbus.bus.EventBus;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.opengl.Display;

import java.util.Calendar;

public class PrefabLove {
    public static final String CLIENT_NAME = "PrefabLove";
    public static final String CLIENT_VERSION = "B1";
    public static Logger LOGGER = LogManager.getLogger(PrefabLove.class);
    public static EventBus eventBus = new EventBus();
    public static Calendar calendar = Calendar.getInstance();
    public static ModuleManager moduleManager;
    public static FontManager fontManager;
    public static RenderManager renderManager;
    public static FileManager fileManager;
    public static StyleManager styleManager;
    public static CommandManager commandManager;

    public static void start() {
        LOGGER.info("Starting PrefabLove...");
        GlyphFontManager.INSTANCE.initialize();
        RotationUtil.initialize();
        PrefabLove.eventBus.subscribe(MovementUtils.INSTANCE);
        renderManager = new RenderManager();
        fontManager = new FontManager();
        moduleManager = new ModuleManager();
        fileManager = new FileManager();
        commandManager = new CommandManager();
        Livemessage.instance.init();
        new Thread(() -> {
            while (true) {
                try {
                    calendar = Calendar.getInstance();
                    String timeStr = String.format("%02d年 %02d月 %02d日 %02d:%02d:%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.SECOND));
                    String ampm = calendar.get(Calendar.AM_PM) == Calendar.AM ? " AM" : " PM";
                    String text = "人是会死去的，所以我们都只是提前准备好的尸体。与尸体不同的，只是活着的我们都在地球上忍受着呼吸带来的痛苦";

                    Display.setTitle(CLIENT_NAME + " " + CLIENT_VERSION + " | " +
                            "User：FreeUser | Time：" + timeStr + ampm +
                            " | FPS：" + Minecraft.getDebugFPS() + text);

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }, "TimeUpdater").start();
    }

    public static void stop() {
        LOGGER.info("Stopping PrefabLove...");
    }

    public interface MC {
        Minecraft mc = Minecraft.getMinecraft();
    }
}