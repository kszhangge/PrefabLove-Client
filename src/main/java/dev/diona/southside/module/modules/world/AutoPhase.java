package dev.diona.southside.module.modules.world;

import dev.diona.southside.PrefabLove;
import dev.diona.southside.event.events.PacketEvent;
import dev.diona.southside.event.events.WorldEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.player.Blink;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class AutoPhase extends Module {
    public AutoPhase(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }
    boolean hasBlock = false;
    boolean a = false;

    @Override
    public boolean onEnable() {
        hasBlock = false;
        a = false;

        return true;
    }
    @EventListener
    public final void onWorld(final WorldEvent event) {
        hasBlock = false;
    }

    @EventListener
    public final void onPacket(final PacketEvent event) {
        if (event.getPacket() instanceof SPacketChat) {
            String s = ((SPacketChat) event.getPacket()).getChatComponent().getFormattedText();

            if (s.contains("你选择了") && s.contains("附魔师")) {
                hasBlock = true;
            }

            if (s.contains("开始倒计时") && s.contains("1")) {

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Blink blink = (Blink) PrefabLove.moduleManager.getModuleByClass(Blink.class);
                        mc.getConnection().getNetworkManager().sendPacket(new CPacketConfirmTransaction(0, (short) 0,true));
                        blink.setEnable(false);
                        a = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
            if (s.contains("开始倒计时") && s.contains("5")) {
                Blink blink = (Blink) PrefabLove.moduleManager.getModuleByClass(Blink.class);

                if (blink.isEnabled()) {
                    blink.setEnable(false);
                }

                blink.setEnable(true);
                new Thread(() -> {
                    try {
                        Thread.sleep(6000);
                        mc.getConnection().getNetworkManager().sendPacket(new CPacketConfirmTransaction(0, (short) 0,true));
                        blink.setEnable(false);
                        a = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();

                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ), EnumFacing.UP));
                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ), EnumFacing.UP));
                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.player.posX, mc.player.posY - 3, mc.player.posZ), EnumFacing.UP));
                mc.world.setBlockToAir(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ));
                mc.world.setBlockToAir(new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ));
                mc.world.setBlockToAir(new BlockPos(mc.player.posX, mc.player.posY - 3, mc.player.posZ));

                if (hasBlock) {
                    new SPacketSetSlot(0, 37, new ItemStack(Item.getItemFromBlock(Blocks.BOOKSHELF), 16)).processPacket(mc.getConnection());
                }

                a = true;
            }
        }
    }
}
