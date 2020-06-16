package su.grazoon.corona.api.session;

import io.netty.channel.ChannelFuture;
import su.grazoon.corona.api.PayloadPacket;

/**
 * @author glowgrew
 */
public interface Session {

    ChannelFuture sendPacket(PayloadPacket packet);

    void receivePacket(PayloadPacket packet);

    void disconnect();

    void onReady();

    void onDisconnect();
}
