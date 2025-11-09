package dev.diona.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.diona.southside.PrefabLove;

import java.awt.*;

public class WatermarkHud extends Hud {
    private float lastTextWidth = 0;
    private float lastTextHeight = 0;

    public WatermarkHud() {
        super(0, 0, 2f);
    }

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
        nanovg.setupAndDraw(true, vg -> {
            final String tempClientName = PrefabLove.CLIENT_NAME;
            lastTextWidth = nanovg.getTextWidth(vg, tempClientName, 10, Fonts.WQY) + 6;
            lastTextHeight = nanovg.getTextHeight(vg, 10, Fonts.WQY);
            float width = getWidth(scale, example);
            float height = getHeight(scale, example);
            nanovg.drawDropShadow(vg, x, y, width, height, 10, 0F, 5, new Color(0, 0, 0, 127));
            nanovg.drawRoundedRect(vg, x, y, width, height, new Color(0, 0, 0, 100).getRGB() , 5);
            nanovg.drawRoundedRect(vg, x + 5, (float) (y + 2.5 * scale), 1 * scale, height - 5f * scale, Color.WHITE.getRGB(), 1 * scale / 2);
            nanovg.drawDropShadow(vg, x + 5, (float) (y + 2.5 * scale), 1 * scale, height - 5f * scale, 3, 0.01F, 1 * scale / 2, new Color(255, 255, 255, 255));
            nanovg.drawSvg(vg, SVGs.PREFABLOVE, x + (width - lastTextWidth) / 2f + 3f * scale - 17.5f, y + (height - lastTextHeight) / 2.7f + scale - 5.5f,20, 20, Color.WHITE.getRGB(), 100);

            nanovg.drawRawTextWithFormatting(vg, tempClientName, x + (width - lastTextWidth) / 2 + 3F * scale + 2.5f, y + (height - lastTextHeight) / 2.7f + scale, -1, 10, Fonts.WQY);
        });
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return lastTextWidth * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return lastTextHeight * scale;
    }
}
