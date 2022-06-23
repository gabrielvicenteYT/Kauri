package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import cc.funkemunky.api.utils.trans.WrappedClientboundTransactionPacket;
import cc.funkemunky.api.utils.trans.WrappedServerboundTransactionPacket;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.val;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@CheckInfo(name = "BadPackets (N)", description = "Designed to patch disablers for Kauri.",
        checkType = CheckType.BADPACKETS, devStage = DevStage.BETA, vlToFlag = 4)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class BadPacketsN extends Check {
    @Setting(name  = "kickPlayer")
    private static boolean kickPlayer = true;

    private int flying, flying2, lastTick, skipBuffer;
    private short lastId;
    private final Timer lastTrans = new TickTimer(), lastSentTrans = new MillisTimer(),
            lastKeepAlive = new TickTimer(), lastSentKeepAlive = new TickTimer(),
            lastFlying = new TickTimer(), lastSkipFlag = new TickTimer();

    @Setting(name = "keepaliveKick")
    private static boolean keepaliveKicking = true;

    @Setting(name = "strings.kick")
    private static String kickString = "[Kauri] Invalid packets (%s).";

    public BadPacketsN() {
        lastSentTrans.reset();
        lastTrans.reset();
    }

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet) {

        if(lastSentTrans.isNotPassed(300L)
                && ++flying > 305 + (data.lagInfo.ping / 50.)
                && Kauri.INSTANCE.tps.getAverage() > 19.6
                && lastKeepAlive.isNotPassed(4000L)) {
            vl++;
            flag("f=%s lKA=%s t=CANCEL", flying, lastKeepAlive.getPassed());
        }

        lastFlying.reset();
    }

    @Event
    public void onEvent(PlayerMoveEvent event) {
        if(flying > 10) {
            event.setCancelled(true);
        }
    }

    @Event
    public void onEvent(EntityDamageByEntityEvent event) {
        if(flying > 10) {
            event.setCancelled(true);
        }
    }

    @Event
    public void onEvent(PlayerInteractEvent event) {
        if(flying > 10) {
            event.setCancelled(true);
        }
    }

    @Packet
    public void onOutKeepalive(WrapperPlayServerKeepAlive packet) {
        lastSentKeepAlive.reset();
    }

    @Packet
    public void onKeepalive(WrapperPlayClientKeepAlive packet) {
        lastKeepAlive.reset();
    }

    @Packet
    public void onOutTrans(WrappedClientboundTransactionPacket packet) {
        lastSentTrans.reset();
    }

    @Packet
    public void onTransaction(WrappedServerboundTransactionPacket packet, long now) {
        if(packet.getWindow() != 0) return;

        val response
                = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getActionId());

        if (response.isPresent()) {
            flying = 0;
            lastTrans.reset();
        }
    }
}
