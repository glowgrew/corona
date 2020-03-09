package su.grazoon.corona.common;

import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.common.packet.AlertPacket;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class PayloadPacketRegistry {

    private final PacketData[] registeredPackets;
    private final Map<Class<? extends PayloadPacket>, PacketData> packetDataByType;

    public PayloadPacketRegistry() {
        this.registeredPackets = new PacketData[255];
        this.packetDataByType = new HashMap<>();

        registerPacket(1, AlertPacket::new);
    }

    public void registerPacket(int id, Supplier<PayloadPacket> creator) {
        PacketData packetData = new PacketData(creator, creator.get().getClass(), id);
        registeredPackets[id] = packetData;
        packetDataByType.put(packetData.type, packetData);
    }

    public PacketData getPacketData(int id) {
        checkState(id < 255, "The id is bigger than maximum packet size");
        PacketData packet = registeredPackets[id];
        checkNotNull(packet, "Unknown packet with id %s", id);
        return packet;
    }

    public PacketData getPacketDataByType(Class<? extends PayloadPacket> packet) {
        checkNotNull(packet, "packet");
        checkState(packetDataByType.containsKey(packet), "Unknown packet %s", packet.getName());
        return packetDataByType.get(packet);
    }

    public List<PacketData> getRegisteredPackets() {
        return Arrays.stream(registeredPackets).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static class PacketData {

        private final Supplier<? extends PayloadPacket> creator;
        private final Class<? extends PayloadPacket> type;
        private final int id;

        PacketData(Supplier<? extends PayloadPacket> creator, Class<? extends PayloadPacket> type, int id) {
            this.creator = creator;
            this.type = type;
            this.id = id;
        }

        public Supplier<? extends PayloadPacket> getCreator() {
            return creator;
        }

        public Class<? extends PayloadPacket> getType() {
            return type;
        }

        public int getId() {
            return id;
        }
    }
}
