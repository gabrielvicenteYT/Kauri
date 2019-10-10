package cc.funkemunky.anticheat.api.pup;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Packets;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AntiPUP implements Listener {

    @Getter
    private String name;

    @Getter
    private PuPType type;

    @Getter
    @Setter
    private PlayerData data;

    @Getter
    @Setter
    private boolean enabled;

    public List<String> packets = new ArrayList<>();

    public AntiPUP(String name, PuPType type, boolean enabled) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;

        if(getClass().isAnnotationPresent(Packets.class)) {
            Packets packetsA = getClass().getAnnotation(Packets.class);

            packets.addAll(Arrays.asList(packetsA.packets()));
        }
    }

    public abstract boolean onPacket(Object packet, String packetType, long timeStamp);

    public void setEnabled(boolean enabled) {
        if (this.enabled = enabled) {
            Bukkit.getPluginManager().registerEvents(this, Kauri.getInstance());
        } else {
            HandlerList.unregisterAll(this);
        }
    }
}