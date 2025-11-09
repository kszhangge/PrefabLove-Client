package dev.diona.southside.util.player;

import dev.diona.southside.util.misc.FakePlayer;

import static dev.diona.southside.PrefabLove.MC.mc;

public class FakePlayerUtil {
    public static FakePlayer spawnFakePlayer() {
        if (mc.world == null) return null;
        return new FakePlayer(mc.player);
    }
}
