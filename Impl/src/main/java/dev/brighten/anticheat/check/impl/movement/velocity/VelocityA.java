package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (A)", description = "Checks for vertical velocity modifications.",
        checkType = CheckType.VELOCITY, punishVL = 32, executable = false)
@Cancellable
public class VelocityA extends Check {

    private double vY;
    private long velocityTS;
    
    @Packet
    public void onTransaction(WrappedInKeepAlivePacket packet, long timeStamp) {
        if(packet.getTime() == data.playerInfo.velocityKeepalive) {
            velocityTS = timeStamp;
            vY = data.playerInfo.velocityY;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(vY > 0
                && !data.playerInfo.generalCancel
                && !data.lagInfo.lagging
                && data.playerInfo.worldLoaded
                && !data.blockInfo.inWeb
                && data.lagInfo.lastPacketDrop.hasPassed(5)
                && !data.blockInfo.onClimbable
                && data.playerInfo.blockAboveTimer.hasPassed(6)) {

            double pct = data.playerInfo.deltaY / vY * 100;

            if (pct < 99.999
                    && !data.playerInfo.lastBlockPlace.hasNotPassed(5)
                    && !data.blockInfo.blocksAbove) {
                if (++vl > 15) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 0.25f : 0;

            vY-= 0.08;
            vY*= 0.98;

            if(vY < 0.005 || data.blockInfo.collidesHorizontally
                    || data.blockInfo.collidesVertically) vY = 0;

            debug("pct=" + pct + " vl=" + vl);
        } else vY = 0;
    }
}
