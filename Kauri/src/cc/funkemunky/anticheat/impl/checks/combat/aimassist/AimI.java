package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;

@Packets(packets = {
        Packet.Client.LOOK,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK,})
public class AimI extends Check {

    private final Deque<Float> pitchDeque = new LinkedList<>();
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val from = this.getData().getMovementProcessor().getFrom();
        val to = this.getData().getMovementProcessor().getTo();

        val yawChange = Math.abs(from.getYaw() - to.getYaw());
        val pitchChange = Math.abs(from.getPitch() - to.getPitch());

        pitchDeque.add(pitchChange);

        val pitchAverage = pitchDeque.stream().mapToDouble(Float::doubleValue).average().orElse(0.0F);
        val pitchRatio = pitchAverage / pitchChange;

        if (pitchRatio > 100.F && yawChange > 2.f) {
            if (++vl > 2) {
                this.flag("P: " + pitchRatio, false, false);
            }
        } else {
            vl = 0;
        }

        if (pitchDeque.size() == 20) {
            pitchDeque.clear();
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}