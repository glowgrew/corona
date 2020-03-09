package su.grazoon.corona.api;

import su.grazoon.corona.api.credentials.ConnectionCredentials;

import java.io.IOException;

public interface NettyClient {

    void connect(ConnectionCredentials credentials) throws IOException;

    default void connect(ConnectionCredentials credentials, int attempts) {
        while (attempts > -1) {
            attempts--;
            try {
                connect(credentials);
            } catch (IOException e) {
                continue;
            }
            break;
        }
    }

    default void connect(ConnectionCredentials credentials, int attempts, long sleepTime) {
        while (attempts > -1) {
            attempts--;
            try {
                connect(credentials);
                Thread.sleep(sleepTime);
            } catch (IOException e) {
                continue;
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread was interrupted.");
            }
            break;
        }
    }

    void shutdown();

    void sendPacket(PayloadPacket payloadPacket);

    PayloadPacketHandler packetHandler();
}
