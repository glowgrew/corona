package su.grazoon.corona.api.credentials;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a connection data used by client or server
 *
 * @author glowgrew
 */
public class ConnectionCredentials {

    private final String hostname;
    private final int port;
    private final String server;

    public ConnectionCredentials(String hostname, int port, String server) {
        this.hostname = checkNotNull(hostname);
        this.port = port;
        this.server = checkNotNull(server);
    }

    public String getFormattedAddress() {
        return hostname + ":" + port;
    }

    @NotNull
    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @NotNull
    public String getServer() {
        return server;
    }
}
