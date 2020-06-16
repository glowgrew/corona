package su.grazoon.corona.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.api.credentials.ConnectionCredentialsFactory;
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.ConfigConnectionCredentialsFactory;
import su.grazoon.corona.common.packet.AlertPacket;
import su.grazoon.corona.server.NativeNettyServer;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static su.grazoon.corona.api.credentials.SenderType.CORONA;
import static su.grazoon.corona.api.credentials.SenderType.PAPER;

public class NativeNettyClientTest {

    private static ConnectionCredentials paperCredentials, coronaCredentials;

    @BeforeAll
    static void beforeAll() {
        ConnectionCredentialsFactory credentialsFactory = new ConfigConnectionCredentialsFactory(new DefaultCoronaConfig(
                Paths.get("src/test/resources"),
                "test-config.conf"));
        paperCredentials = credentialsFactory.create(PAPER);
        coronaCredentials = credentialsFactory.create(CORONA);
    }

    @Test
    public void connectionTest() {
        NativeNettyServer server = new NativeNettyServer(1, 1);
        NativeNettyClient client = new NativeNettyClient(1);

        server.bind(coronaCredentials);
        client.connect(paperCredentials);

        server.shutdown();
        client.shutdown();
    }


    @Test
    public void sendPacketTest() throws InterruptedException {
        NativeNettyServer server = new NativeNettyServer(1, 1);
        NativeNettyClient client = new NativeNettyClient(1);

        server.bind(coronaCredentials);
        client.connect(paperCredentials);

        AtomicBoolean clientWrapper = new AtomicBoolean(false);
        AtomicBoolean serverWrapper = new AtomicBoolean(false);

        server.packetHandler().registerHandler(AlertPacket.class, packet -> {
            System.out.println("ALERT FROM CLIENT! - " + packet.getMessage());
            clientWrapper.set(true);
        });

        server.packetHandler().registerHandler(AlertPacket.class, packet -> {
            System.out.println(packet.getMessage());
            serverWrapper.set(true);
        });

        client.sendPacket(new AlertPacket("1"));
        server.sendPacket(new AlertPacket("Hello from CoronaApi!"));

        Thread.sleep(200);

        server.shutdown();
        client.shutdown();

        assertTrue(clientWrapper.get());
        assertTrue(serverWrapper.get());
    }
}
