package su.grazoon.corona.common.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;
import su.grazoon.corona.api.session.Session;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author glowgrew
 */
public class CoronaSession implements Session {

    private final Channel channel;
    private final PayloadPacketHandler packetHandler;
    private boolean active;

    public CoronaSession(Channel channel, PayloadPacketHandler packetHandler) {
        this.channel = channel;
        this.packetHandler = packetHandler;
        this.active = true;
    }

    public Channel getChannel() {
        return channel;
    }

    public PayloadPacketHandler getPacketHandler() {
        return packetHandler;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public ChannelFuture sendPacket(PayloadPacket packet) {
        checkState(active, "Channel is not active");
        return channel.writeAndFlush(packet);
    }

    @Override
    public void receivePacket(PayloadPacket packet) {
        checkState(active, "Channel is not active");
        packetHandler.acceptPacket(packet);
    }

    @Override
    public void disconnect() {
        channel.close();
    }

    @Override
    public void onReady() {
        // ...
    }

    @Override
    public void onDisconnect() {
        // ...
    }
}
