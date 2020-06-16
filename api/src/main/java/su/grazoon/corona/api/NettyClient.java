package su.grazoon.corona.api;

import io.netty.channel.ChannelFuture;
import su.grazoon.corona.api.credentials.ConnectionCredentials;

public interface NettyClient {

    void connect(ConnectionCredentials credentials);

    void shutdown();

    ChannelFuture sendPacket(PayloadPacket payloadPacket);

    PayloadPacketHandler packetHandler();
}
