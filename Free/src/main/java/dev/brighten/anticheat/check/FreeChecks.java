package dev.brighten.anticheat.check;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerA;
import dev.brighten.anticheat.check.impl.combat.hitbox.ReachA;
import dev.brighten.anticheat.check.impl.movement.fly.FlyA;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallA;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedA;
import dev.brighten.anticheat.check.impl.packets.badpackets.*;

@Init(priority = Priority.LOWEST)
public class FreeChecks {
    
    public FreeChecks() {
        Check.register(new AutoclickerA());
        Check.register(new FlyA());
        Check.register(new NoFallA());
        Check.register(new ReachA());
        Check.register(new SpeedA());
        Check.register(new BadPacketsA());
        Check.register(new BadPacketsB());
        Check.register(new BadPacketsC());
        Check.register(new BadPacketsD());
        Check.register(new BadPacketsE());
        Check.register(new BadPacketsF());
        Check.register(new BadPacketsG());
        Check.register(new BadPacketsH());
        Check.register(new BadPacketsI());
        Check.register(new BadPacketsK());
        Check.register(new BadPacketsL());
        Check.register(new BadPacketsM());
    }
}