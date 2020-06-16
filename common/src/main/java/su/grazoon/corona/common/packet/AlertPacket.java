package su.grazoon.corona.common.packet;

import su.grazoon.corona.common.ClientPayloadPacket;
import su.grazoon.corona.common.CoronaPacketBuffer;

/**
 * @author glowgrew
 */
public class AlertPacket extends ClientPayloadPacket {

    private String message;

    public AlertPacket(String message) {
        this.message = message;
    }

    public AlertPacket() {
    }

    @Override
    protected void write0(CoronaPacketBuffer buffer) {
        buffer.writeString(message);
    }

    @Override
    protected void read0(CoronaPacketBuffer buffer) {
        message = buffer.readString(32767);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
