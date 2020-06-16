package su.grazoon.corona.server;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.NettyServer;
import su.grazoon.corona.api.config.CoronaConfig;
import su.grazoon.corona.api.credentials.ConnectionCredentialsFactory;
import su.grazoon.corona.api.credentials.SenderType;
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.ConfigConnectionCredentialsFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author glowgrew
 */
public class Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws IOException {
        System.setProperty("io.netty.leakDetectionLevel", "advanced");

        CoronaConfig config = new DefaultCoronaConfig(Paths.get("data"), "config.conf");
        NettyServer server = new NativeNettyServer(config.getInt("boss-thread-amount", 2),
                                                   config.getInt("worker-thread-amount", 4));
        ConnectionCredentialsFactory credentialsFactory = new ConfigConnectionCredentialsFactory(config);
        server.bind(credentialsFactory.create(SenderType.CORONA));

        log.info("Welcome to CoronaApi console! Write 'shutdown' to stop CoronaApi");
        log.info("and to close all active connections.");
        Terminal terminal = TerminalBuilder.builder().dumb(true).jna(false).build();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        try {
            String line;
            while ((line = lineReader.readLine("> ")) != null) {
                try {
                    if (line.equals("shutdown")) {
                        server.shutdown();
                        return;
                    }
                    log.info(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (UserInterruptException e) {
            server.shutdown();
        }
    }
}
