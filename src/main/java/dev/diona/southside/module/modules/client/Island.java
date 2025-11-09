package dev.diona.southside.module.modules.client;

import cc.polyfrost.oneconfig.config.options.impl.HUD;
import dev.diona.southside.gui.hud.islandhud.IslandHud;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.annotations.DefaultEnabled;

@DefaultEnabled
public class Island extends Module {
    public final HUD hud = new HUD("Island", new IslandHud());

    public Island() {
        super("Island", "灵动岛", Category.Client, false);
    }
}