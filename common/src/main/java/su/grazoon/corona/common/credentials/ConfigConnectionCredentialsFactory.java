package su.grazoon.corona.common.credentials;

import su.grazoon.corona.api.config.CoronaConfig;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.api.credentials.ConnectionCredentialsFactory;
import su.grazoon.corona.api.credentials.SenderType;

/**
 * @author glowgrew
 */
public class ConfigConnectionCredentialsFactory implements ConnectionCredentialsFactory {

    private final CoronaConfig config;

    public ConfigConnectionCredentialsFactory(CoronaConfig config) {
        this.config = config;
    }

    @Override
    public ConnectionCredentials create(SenderType senderType) {
        String hostname = config.getString("corona.hostname", "localhost");
        int port = config.getInt("corona.port", 5555);
        String serverName = config.getString("corona.server");
        return new CoronaConnectionCredentials(hostname, port, serverName, senderType);
    }
}
