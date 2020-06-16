package su.grazoon.corona.common.packet;

import su.grazoon.corona.api.credentials.SenderType;
import su.grazoon.corona.common.ClientPayloadPacket;
import su.grazoon.corona.common.CoronaPacketBuffer;
import su.grazoon.corona.common.server.Server;
import su.grazoon.corona.common.server.ServerGroup;
import su.grazoon.corona.common.server.ServerProfile;

/**
 * @author glowgrew
 */
public class HandshakePacket extends ClientPayloadPacket {

    private SenderType type;
    private Server server;

    public HandshakePacket(SenderType type, Server server) {
        this.type = type;
    }

    public HandshakePacket() {
    }

    @Override
    protected void write0(CoronaPacketBuffer buffer) {
        buffer.writeEnumValue(type);
        buffer.writeEnumValue(server.getProfile().getGroup());
        buffer.writeString(server.getProfile().getName());
        buffer.writeVarInt(server.getOnlinePlayers());
        buffer.writeVarInt(server.getMaxPlayers());
    }

    @Override
    protected void read0(CoronaPacketBuffer buffer) {
        type = buffer.readEnumValue(SenderType.class);

        server = new Server(new ServerProfile(buffer.readEnumValue(ServerGroup.class), buffer.readString(32767)),
                            buffer.readVarInt(),
                            buffer.readVarInt());
    }

    public SenderType getType() {
        return type;
    }

    public void setType(SenderType type) {
        this.type = type;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}

