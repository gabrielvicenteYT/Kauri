package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.ComplexCollisionBox;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.AtomicDouble;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 15)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(
            EntityType.ZOMBIE,
            EntityType.VILLAGER,
            EntityType.PLAYER,
            EntityType.SKELETON,
            EntityType.PIG_ZOMBIE,
            EntityType.WITCH,
            EntityType.CREEPER,
            EntityType.ENDERMAN);

    @Setting(name = "allowNPCFlag")
    private static boolean allowNPCFlag = true;

    @Packet
    public void onFlying(WrappedInUseEntityPacket packet, long timeStamp) {
        if (checkParameters(data)) {

            List<RayCollision> rayTrace = Stream.of(data.playerInfo.to.clone(), data.playerInfo.from.clone())
                    .map(l -> {
                        KLocation loc = l.clone();
                        loc.y+=data.playerInfo.sneaking ? 1.54f : 1.62f;
                        return new RayCollision(loc.toVector(),
                                MathUtils.getDirection(loc));
                    })
                    .collect(Collectors.toList());

            List<SimpleCollisionBox> entityLocations = new ArrayList<>();


           data.targetPastLocation
                    .getEstimatedLocation(data.lagInfo.transPing * 50, 150
                                    + Math.abs(data.lagInfo.transPing - data.lagInfo.lastTransPing))
                    .stream()
                    .map(loc -> getHitbox(loc, data.target))
                    .forEach(box -> box.downCast(entityLocations));

            long collisions = 0;
            AtomicDouble distance = new AtomicDouble(10);

            for (RayCollision ray : rayTrace) {
                collisions+= entityLocations.stream().filter(bb -> {
                    Vector point;
                    if((point = ray.collisionPoint(bb)) != null) {
                        double dist = point.distance(ray.getOrigin());

                        distance.set(Math.min(dist, distance.get()));
                        return dist < 3.65f;
                    }
                    return false;
                }).count();
            }

            if (collisions == 0
                    && timeStamp - data.creation > 3000L
                    && data.lagInfo.lastPingDrop.hasPassed(10)
                    && data.lagInfo.lastPacketDrop.hasPassed(4)) {
                if(vl++ > 10)  flag("distance=%v ping=%p tps=%t",
                        distance.get() != -1 ? distance.get() : "[none collided]");
            } else vl -= vl > 0 ? 0.25 : 0;

            debug("collided=" + collisions + " distance=" + distance.get() + " type=" + data.target.getType());
        }
    }

    private static boolean checkParameters(ObjectData data) {
        return data.playerInfo.lastAttack.hasNotPassed(0)
                && data.target != null
                && data.targetPastLocation.previousLocations.size() > 12
                && Kauri.INSTANCE.lastTickLag.hasPassed(10)
                && allowedEntities.contains(data.target.getType())
                && !data.playerInfo.creative
                && data.playerInfo.lastTargetSwitch.hasPassed()
                && !data.getPlayer().getGameMode().equals(GameMode.CREATIVE);
    }

    private static CollisionBox getHitbox(KLocation loc, Entity type) {
        List<SimpleCollisionBox> boxes = new ArrayList<>();

        EntityData.getEntityBox(loc, type).downCast(boxes);

        boxes.forEach(box -> box.expand(0.2));

        if(boxes.size() == 1) return boxes.get(0);

        return new ComplexCollisionBox(boxes.toArray(new SimpleCollisionBox[0]));
    }
}