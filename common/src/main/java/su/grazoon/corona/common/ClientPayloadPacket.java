package su.grazoon.corona.common;

import io.netty.buffer.ByteBuf;
import su.grazoon.corona.api.PayloadPacket;

public abstract class ClientPayloadPacket implements PayloadPacket {

    protected abstract void write0(CoronaPacketBuffer buffer);

    protected abstract void read0(CoronaPacketBuffer buffer);

    @Override
    public final void write(ByteBuf buffer) {
        write0(new CoronaPacketBuffer(buffer));
    }

    @Override
    public final void read(ByteBuf buffer) {
        read0(new CoronaPacketBuffer(buffer));
    }
}
