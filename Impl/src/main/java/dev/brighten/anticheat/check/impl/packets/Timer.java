package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Timer", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, punishVL = 175)
@Cancellable
public class Timer extends Check {

    private long lastTS, lRange;
    private EvictingList<Long> times = new EvictingList<>(20);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        long elapsed = timeStamp - lastTS;

        if(timeStamp - data.creation > 2000
                && timeStamp - data.playerInfo.lastServerPos > 80L) {
            times.add(elapsed);
            val summary = times.stream().mapToLong(val -> val).summaryStatistics();
            double average = summary.getAverage();
            double ratio = 50 / average;
            long range = summary.getMax() - summary.getMin();
            double pct = ratio * 100;

            if(!Double.isNaN(pct) && !Double.isInfinite(pct)) {

                if ((pct > 100.4D)
                        && data.lagInfo.lastPingDrop.hasPassed(30)
                        && Kauri.INSTANCE.lastTickLag.hasPassed(5)
                        && Kauri.INSTANCE.tps > 18.5) {
                    //Maybe lower threshold? I do not think it needs that high of one.
                    if (vl++ > 65) flag("pct=%v.2", pct);
                } else vl -= vl > 0 ? 1.5 : 0;

                debug("pct=%v.2 vl=%v.1 elapsed=%vms avg=%v.2 range=%v.2", pct, vl, elapsed, average, range);
            lRange = range;
            }
        }
        lastTS = timeStamp;
    }
}