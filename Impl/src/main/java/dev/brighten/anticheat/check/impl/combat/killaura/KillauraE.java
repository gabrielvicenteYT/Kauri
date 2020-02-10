package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.EntityType;

@CheckInfo(name = "Killaura (E)", description = "Checks if a user is sprinting while attacking a player.",
        checkType = CheckType.KILLAURA, punishVL = 20, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraE extends Check {

    private boolean attacked;
    private float lmoveForward;
    private double lastDeltaXZ, verbose;
    private TickTimer lastKeyChange = new TickTimer(4);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(lmoveForward != data.predictionService.moveForward) lastKeyChange.reset();
        if(attacked) {
            if(data.target != null
                    && data.target.getType().equals(EntityType.PLAYER)
                    && data.playerInfo.lastVelocity.hasPassed(4)
                    && data.predictionService.moveForward > 0
                    && data.playerInfo.deltaXZ > 0.1
                    && data.playerInfo.sprinting
                    && lastKeyChange.hasPassed(3)
                    && data.playerInfo.deltaXZ >= lastDeltaXZ) {
                if(data.lagInfo.lastPingDrop.hasPassed(5) && verbose++ > 3) {
                    vl++;
                    flag("dxz=%1 ldxz=%2", MathUtils.round(data.playerInfo.deltaXZ, 2),
                            MathUtils.round(lastDeltaXZ, 2));
                }
            } else verbose-= verbose > 0 ? 0.05 : 0;
            debug("deltaXZ=" + data.playerInfo.deltaXZ + " ldxz=" + lastDeltaXZ);
            attacked = false;
        } else lastDeltaXZ = data.playerInfo.deltaXZ;
        lmoveForward = data.predictionService.moveForward;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        attacked = true;
    }

}
