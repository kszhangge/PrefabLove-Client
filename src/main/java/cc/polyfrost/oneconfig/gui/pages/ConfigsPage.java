package cc.polyfrost.oneconfig.gui.pages;

import cc.polyfrost.oneconfig.gui.elements.Dropdown;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigDropdown;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;


public class ConfigsPage extends Page {
    // 等待大蛇完成
    public ConfigsPage() {
        super("Configs");
    }


    @Override
    public void draw(long vg, int x, int y, InputHandler inputHandler) {
        final NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        nanoVGHelper.drawText(vg,"Configs",x + 64,y + 64,-1,24, Fonts.SEMIBOLD);


    }
    @Override
    public boolean isBase() {
        return true;
    }
}
