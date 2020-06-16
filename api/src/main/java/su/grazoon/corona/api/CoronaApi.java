package su.grazoon.corona.api;

import io.netty.channel.ChannelFuture;

/**
 * @author glowgrew
 */
public interface CoronaApi {

    static CoronaApi api() {
        return CoronaApiProvider.get();
    }

    ChannelFuture sendPacket(PayloadPacket payloadPacket);

    NettyClient getClient();
}
