package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@Packets(packets = {
        Packet.Client.LOOK,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK,})
public class AimH extends Check {

    public AimH(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (getData().getLastServerPos().hasNotPassed(4) && getData().getLastLogin().hasNotPassed(20)) return;

        Vector vector = new Vector(move.getTo().getX() - move.getFrom().getX(), 0, move.getTo().getZ() - move.getFrom().getZ());
        double angleMove = vector.distanceSquared((new Vector(move.getTo().getYaw() - move.getFrom().getYaw(), 0, move.getTo().getYaw() - move.getFrom().getYaw())));

        if (angleMove > 100000 && move.getDeltaXZ() > 0.2f && move.getDeltaXZ() < 1) {
            flag("angle: " + angleMove, true, true);
        }

        debug("angle: " + angleMove);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}