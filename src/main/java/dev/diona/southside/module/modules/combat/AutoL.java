package dev.diona.southside.module.modules.combat;

import dev.diona.southside.PrefabLove;
import dev.diona.southside.event.EventState;
import dev.diona.southside.event.events.AttackEvent;
import dev.diona.southside.event.events.MotionEvent;
import dev.diona.southside.event.events.WorldEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.client.Target;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.diona.southside.module.modules.misc.AutoReport;
import dev.diona.southside.util.misc.TextUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class AutoL extends Module {
    public final Slider delay = new Slider("Delay", 0, 0, 50, 1);

    public AutoL(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    private final String[] zhengZhengRiShang = {
            "一破，卧龙出山",
            "双连，一战成名",
            "三连，举世皆惊",
            "四连，天下无敌"
    };

    private EntityPlayer target;
    private int ticks;

    private int kills = 0;

    @EventListener
    public void onWorld(WorldEvent event) {
        target = null;
        kills = 0;
    }


    @EventListener
    public void onMotion(MotionEvent event) {
        if (event.getState() == EventState.POST) return;
        if (mc.player.isSpectator()) {
            target = null;
        }

        if (mc.getCurrentServerData() == null || mc.getCurrentServerData().serverIP.equals("mc.loyisa.cn") || mc.getCurrentServerData().serverIP.equals("mc.loyisa.eu")) return;

        if (target != null && !mc.world.playerEntities.contains(target) && target.isDead) {
            if (ticks >= delay.getValue().intValue() + Math.random() * 5) {
                String insult = "@" + target.getName() + " ";

                kills++;
                if (kills <= 4) {
                    insult += zhengZhengRiShang[kills - 1];
                } else {
                    insult += TextUtil.read(kills) + "连，诛天灭地";

                }
                insult += " 你已被 southside 客户端击毙";
                if (PrefabLove.moduleManager.getModuleByClass(AutoReport.class).isEnabled()) {
                    if (!Target.isTarget(target)) {
                        mc.player.sendChatMessage("/report " + target.getName());
                        AutoReport.targetName = target.getName();
                    }
                }
                mc.player.sendChatMessage(insult);
                target = null;
            }
            ticks++;
        }
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        final Entity target = event.getTargetEntity();

        if (target instanceof EntityPlayer && Target.isTarget(target)) {
            this.target = (EntityPlayer) target;
            ticks = 0;
        }
    }
}
