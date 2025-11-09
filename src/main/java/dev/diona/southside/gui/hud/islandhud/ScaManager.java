package dev.diona.southside.gui.hud.islandhud;

import dev.diona.southside.module.modules.world.Scaffold;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ScaManager {
    private static ScaManager instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    
    private ScaManager() {}
    
    public static ScaManager getInstance() {
        if (instance == null) {
            instance = new ScaManager();
        }
        return instance;
    }
    
    public boolean isScaffoldActive() {
        return Scaffold.INSTANCE != null && Scaffold.INSTANCE.isEnabled();
    }
    
    public int getBlockCount() {
        if (Scaffold.INSTANCE == null) return 0;
        return Scaffold.INSTANCE.getBlockCount();
    }
    
    public ItemStack getCurrentBlock() {
        if (mc.player == null) return null;
        
        ItemStack heldItem = mc.player.getHeldItemMainhand();
        if (heldItem != null && heldItem.getItem() instanceof ItemBlock block) {
            if (isValidBlock(block)) {
                return heldItem;
            }
        }
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock block) {
                if (isValidBlock(block)) {
                    return stack;
                }
            }
        }
        
        return null;
    }
    
    private boolean isValidBlock(ItemBlock block) {
        return !Scaffold.invalidBlocks.contains(block.getBlock());
    }
    
    public double getBPS() {
        if (mc.player == null) return 0.0;
        double deltaX = mc.player.posX - mc.player.prevPosX;
        double deltaZ = mc.player.posZ - mc.player.prevPosZ;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20.0;
    }
}
