package su.grazoon.corona.common;

import io.netty.channel.ChannelFuture;
import su.grazoon.corona.api.CoronaApi;
import su.grazoon.corona.api.NettyClient;
import su.grazoon.corona.api.PayloadPacket;

/**
 * @author glowgrew
 */
public class CoronaApiImpl implements CoronaApi {

    private final NettyClient nettyClient;

    public CoronaApiImpl(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public ChannelFuture sendPacket(PayloadPacket payloadPacket) {
        return nettyClient.sendPacket(payloadPacket);
    }

    @Override
    public NettyClient getClient() {
        return nettyClient;
    }
}
