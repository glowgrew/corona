package su.grazoon.corona.client.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.java.annotation.plugin.author.Authors;
import su.grazoon.corona.api.NettyClient;
import su.grazoon.corona.api.config.CoronaConfig;
import su.grazoon.corona.api.credentials.ConnectionCredentialsFactory;
import su.grazoon.corona.client.NativeNettyClient;
import su.grazoon.corona.common.config.DefaultCoronaConfig;
import su.grazoon.corona.common.credentials.HoconConnectionCredentialsFactory;
import su.grazoon.corona.common.packet.AlertPacket;

/**
 * @author glowgrew
 */
@Plugin(name = "CoronaBukkit", version = "1.0.0")
@Authors({@Author("glowgrew"), @Author("DokanBoy")})
public class CoronaBukkitPlugin extends JavaPlugin {

    private NettyClient client;

    @Override
    public void onEnable() {
        CoronaConfig config = new DefaultCoronaConfig(getDataFolder().toPath(), "config.conf", true);
        ConnectionCredentialsFactory credentialsFactory = new HoconConnectionCredentialsFactory(config);
        client = new NativeNettyClient(4);
        client.connect(credentialsFactory.create(), 5, 200L);
        client.sendPacket(new AlertPacket(1));
    }

    @Override
    public void onDisable() {
        client.shutdown();
    }
}
