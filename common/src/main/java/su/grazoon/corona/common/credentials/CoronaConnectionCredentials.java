package su.grazoon.corona.common.credentials;

import org.jetbrains.annotations.NotNull;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.api.credentials.SenderType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author glowgrew
 */
public class CoronaConnectionCredentials implements ConnectionCredentials {

    private final String hostname;
    private final int port;
    private final String server;
    private final SenderType senderType;

    public CoronaConnectionCredentials(String hostname, int port, String server, SenderType senderType) {
        this.hostname = checkNotNull(hostname);
        this.port = port;
        this.server = checkNotNull(server);
        this.senderType = checkNotNull(senderType);
    }

    @Override
    @NotNull
    public String getFormattedAddress() {
        return hostname + ":" + port;
    }

    @Override
    @NotNull
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    @NotNull
    public String getServer() {
        return server;
    }

    @Override
    @NotNull
    public SenderType getSenderType() {
        return senderType;
    }
}
