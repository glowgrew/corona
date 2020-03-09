package su.grazoon.corona.server;

import su.grazoon.corona.api.NettyServer;
import su.grazoon.corona.api.config.CoronaConfig;
import su.grazoon.corona.api.credentials.ConnectionCredentialsFactory;
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.HoconConnectionCredentialsFactory;

import java.nio.file.Paths;

/**
 * @author glowgrew
 */
public class Bootstrap {

    public static void main(String[] args) {
        NettyServer server = new NativeNettyServer(2, 4);
        CoronaConfig config = new DefaultCoronaConfig(Paths.get("data"), "config.conf", true);
        ConnectionCredentialsFactory credentialsFactory = new HoconConnectionCredentialsFactory(config);
        server.bind(credentialsFactory.create());
    }
}
