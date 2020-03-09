package su.grazoon.corona.api;

import su.grazoon.corona.api.credentials.ConnectionCredentials;

public interface NettyServer {

    void bind(ConnectionCredentials credentials);

    void bindAndLock(ConnectionCredentials credentials);

    void shutdown();

    PayloadPacketHandler packetHandler();
}
