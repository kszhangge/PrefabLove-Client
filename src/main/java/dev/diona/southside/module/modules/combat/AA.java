package dev.diona.southside.module.modules.combat;

import dev.diona.southside.event.events.MotionEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.MathUtil;
import dev.diona.southside.util.player.Rotation;
import dev.diona.southside.util.player.RotationUtil;
import me.bush.eventbus.annotation.EventListener;
import org.apache.commons.lang3.time.StopWatch;

/**
 * @author EzDiaoL
 * @since 15.06.2024
 */
public class AA extends Module {
    public AA(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    private StopWatch stopWatch = new StopWatch();
    private boolean flag = false;

    @Override
    public boolean onEnable() {
        return super.onEnable();
    }

    @Override
    public boolean onDisable() {
        return super.onDisable();
    }

    @EventListener
    public void onMotion(MotionEvent event) {
        if (RotationUtil.targetRotation == null) {
            Rotation rotation = new Rotation(mc.player.rotationYaw - 180 + MathUtil.getRandomInRange(-35, 35), 90);
            rotation = RotationUtil.turn(rotation, "Normal", 180);
            if (rotation != null) {
                RotationUtil.setTargetRotation(rotation, 0);
                mc.gameSettings.keyBindSprint.setPressed(false);
                mc.player.setSprinting(false);
            }
        }
    }
}
