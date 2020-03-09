package su.grazoon.corona.api;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

public interface PayloadPacket extends Serializable {

    void write(ByteBuf buffer);

    void read(ByteBuf buffer);

    default void handle(PayloadPacketHandler handler) {
        handler.acceptPacket(this);
    }
}
