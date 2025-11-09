package dev.diona.southside.module.modules.client;

import cc.polyfrost.oneconfig.config.options.impl.HUD;
import dev.diona.southside.gui.hud.WatermarkHud;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;

public class Watermark extends Module {
    public final HUD hud = new HUD("Watermark", new WatermarkHud());
    public Watermark() {
        super("Watermark", "水印", Category.Client, false);
    }
}