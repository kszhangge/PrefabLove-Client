package dev.diona.southside.module.modules.misc.hackerdetector.check;

import dev.diona.southside.event.events.PacketEvent;

public interface PacketCheck extends Check {
    void onPacket(PacketEvent event);
}
