package su.grazoon.corona.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.HoconConnectionCredentialsFactory;

import java.nio.file.Paths;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author glowgrew
 */
class HoconConnectionCredentialsFactoryTest {

    private static Function<String, ConnectionCredentials> credentialsCreatorStrategy;

    @BeforeAll
    static void beforeAll() {
        credentialsCreatorStrategy = fileName -> new HoconConnectionCredentialsFactory(
                new DefaultCoronaConfig(Paths.get("src/test/resources"), fileName, false)).create();
    }

    @Test
    void testEveryNodeIsSet() {
        ConnectionCredentials credentials = credentialsCreatorStrategy.apply("case-every-node-is-set.conf");

        assertEquals(credentials.getHostname(), "foo");
        assertEquals(credentials.getPort(), 5555);
        assertEquals(credentials.getServer(), "hub");
    }

    @Test
    void testHostnameNodeIsMissing() {
        ConnectionCredentials credentials = credentialsCreatorStrategy.apply("case-hostname-node-is-missing.conf");

        assertEquals(credentials.getHostname(), "localhost");
        assertEquals(credentials.getPort(), 54545);
        assertEquals(credentials.getServer(), "bar");
    }
}