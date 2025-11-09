package dev.diona.southside.gui.hud.islandhud;

import dev.diona.southside.PrefabLove;
import dev.diona.southside.module.modules.combat.AutoGapple;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GappleManager {
    private static GappleManager instance;
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public static GappleManager getInstance() {
        if (instance == null) {
            instance = new GappleManager();
        }
        return instance;
    }
    
    public boolean isGappleActive() {
        AutoGapple gapple = (AutoGapple) PrefabLove.moduleManager.getModuleByClass(AutoGapple.class);
        return gapple != null && gapple.isEnabled();
    }
    
    public int getEatTicks() {
        AutoGapple gapple = (AutoGapple) PrefabLove.moduleManager.getModuleByClass(AutoGapple.class);
        if (gapple != null && gapple.isEnabled()) {
            return gapple.c03s;
        }
        return 0;
    }
    
    public int getGappleCount() {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.GOLDEN_APPLE) {
                count += stack.getCount();
            }
        }
        return Math.min(count, 64);
    }
}
