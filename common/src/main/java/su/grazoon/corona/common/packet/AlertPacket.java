package su.grazoon.corona.common.packet;

import su.grazoon.corona.common.ClientPayloadPacket;
import su.grazoon.corona.common.CoronaPacketBuffer;

/**
 * @author glowgrew
 */
public class AlertPacket extends ClientPayloadPacket {

    public int a;

    public AlertPacket(int a) {
        this.a = a;
    }

    public AlertPacket() {

    }

    @Override
    protected void write0(CoronaPacketBuffer buffer) {
        buffer.writeVarInt(a);
    }

    @Override
    protected void read0(CoronaPacketBuffer buffer) {
        buffer.readVarInt();
    }
}
