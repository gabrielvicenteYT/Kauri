package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Speed (Type C)", description = "Ensures that the acceleration of a player is normal.", type = CheckType.SPEED)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class SpeedC extends Check {

    /*
    What could cause false positives: water, ladders, slimes, initial grounding or jumping, ice.
     */

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val decel = move.isServerOnGround() ? getData().getBlockBelow() != null ? ReflectionsUtil.getFriction(getData().getBlockBelow()) : 0.6 : (getData().getActionProcessor().isSprinting() ? 0.026f : 0.02f);
        val difference = MathUtils.getDelta(move.getLastDeltaXZ(), move.getDeltaXZ());
        val decelDif = MathUtils.getDelta(decel, difference);

        if(move.getAirTicks() > 3 && !getData().isLagging() && getData().getLastLag().hasPassed(5) && decelDif > 0.03 && !MiscUtils.cancelForFlight(getData(), 15, false)) {
            if(vl++ > 4) {
                flag(difference + ">-" + decel, true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;

        debug("decel=" + decel + " difference=" + difference + " vl=" + vl + " decelDif=" + decelDif);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}