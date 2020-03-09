package su.grazoon.corona.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.HoconConnectionCredentialsFactory;
import su.grazoon.corona.common.packet.AlertPacket;
import su.grazoon.corona.server.NativeNettyServer;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeNettyClientTest {

    private static ConnectionCredentials credentials;

    @BeforeAll
    static void beforeAll() {
        credentials = new HoconConnectionCredentialsFactory(
                new DefaultCoronaConfig(Paths.get("src/test/resources"), "test-config.conf", false)).create();
    }

    @Test
    public void connectionTest() {
        NativeNettyServer server = new NativeNettyServer(1, 1);
        NativeNettyClient client = new NativeNettyClient(1);

        server.bind(credentials);
        client.connect(credentials, 5, 1000L);

        server.shutdown();
        client.shutdown();
    }


    @Test
    public void sendPacketTest() throws InterruptedException {
        NativeNettyServer server = new NativeNettyServer(1, 1);
        NativeNettyClient client = new NativeNettyClient(1);

        server.bind(credentials);
        client.connect(credentials, 5, 1000L);

        AtomicBoolean wrapper = new AtomicBoolean(false);
        server.packetHandler().registerHandler(AlertPacket.class, packet -> {
            assertEquals(1, packet.a);
            wrapper.set(true);
            System.out.println(packet.a);
        });
        client.sendPacket(new AlertPacket(1));

        Thread.sleep(200);

        server.shutdown();
        client.shutdown();

        assertTrue(wrapper.get());
    }
}
