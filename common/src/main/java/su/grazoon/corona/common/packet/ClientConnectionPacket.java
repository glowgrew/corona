package su.grazoon.corona.common.packet;

import su.grazoon.corona.common.ClientPayloadPacket;
import su.grazoon.corona.common.CoronaPacketBuffer;

/**
 * @author glowgrew
 */
public class ClientConnectionPacket extends ClientPayloadPacket {

    private Type type;

    public ClientConnectionPacket(Type type) {
        this.type = type;
    }

    public ClientConnectionPacket() {
    }

    @Override
    protected void write0(CoronaPacketBuffer buffer) {
        buffer.writeEnumValue(type);
    }

    @Override
    protected void read0(CoronaPacketBuffer buffer) {
        buffer.readEnumValue(Type.class);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        BUKKIT,
        VELOCITY,
        UNKNOWN,
        ;
    }
}

