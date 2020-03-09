package su.grazoon.corona.common.credentials;

import su.grazoon.corona.api.config.CoronaConfig;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.api.credentials.ConnectionCredentialsFactory;

/**
 * @author glowgrew
 */
public class HoconConnectionCredentialsFactory implements ConnectionCredentialsFactory {

    private final CoronaConfig config;

    public HoconConnectionCredentialsFactory(CoronaConfig config) {
        this.config = config;
    }

    @Override
    public ConnectionCredentials create() {
        String hostname = config.getString("corona.hostname", "localhost");
        int port = config.getInt("corona.port", 5555);
        String server = config.getString("corona.server");
        return new ConnectionCredentials(hostname, port, server);
    }
}
