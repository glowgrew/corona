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
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.HoconConnectionCredentialsFactory;
import su.grazoon.corona.common.packet.ClientConnectionPacket;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author glowgrew
 */
public class Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws IOException {
        NettyServer server = new NativeNettyServer(2, 4);
        CoronaConfig config = new DefaultCoronaConfig(Paths.get("data"), "config.conf", true);
        ConnectionCredentialsFactory credentialsFactory = new HoconConnectionCredentialsFactory(config);
        server.bind(credentialsFactory.create());
        Terminal terminal = TerminalBuilder.builder().dumb(true).jna(false).build();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        try {
            String line;
            while ((line = lineReader.readLine("> ")) != null) {
                try {
                    if (line.equals("stop")) {
                        server.shutdown();
                        return;
                    }
                    log.info(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (UserInterruptException e2) {
            server.shutdown();
        }
    }
}
