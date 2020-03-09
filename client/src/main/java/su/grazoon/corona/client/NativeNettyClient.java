package su.grazoon.corona.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.NettyClient;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.common.CoronaPacketHandler;
import su.grazoon.corona.common.PayloadPacketHandlerImpl;

import java.io.IOException;
import java.net.ConnectException;

public class NativeNettyClient implements NettyClient {

    private static final Logger log = LoggerFactory.getLogger(NativeNettyClient.class);

    private final PayloadPacketHandler packetHandler;

    private final EventLoopGroup eventExecutors;
    private final Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    public NativeNettyClient(int threadAmount) {
        packetHandler = new PayloadPacketHandlerImpl();

        eventExecutors = new NioEventLoopGroup(threadAmount);

        bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel socketChannel) {
                         ChannelPipeline channelPipeline = socketChannel.pipeline();
                         channelPipeline.addLast("object_encoder", new ObjectEncoder());
                         channelPipeline.addLast("object_decoder", new ObjectDecoder(
                                 ClassResolvers.weakCachingResolver(getClass().getClassLoader())));
                         channelPipeline.addLast("client_handler", new CoronaPacketHandler(packetHandler));
                     }
                 });
    }

    public void connect(ConnectionCredentials credentials) throws IOException {
        channelFuture = bootstrap.connect(credentials.getHostname(), credentials.getPort());
        try {
            channelFuture.sync();
            log.info("Connected to Corona ({})", credentials.getFormattedAddress());
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread was interrupted.");
        }
        if (!channelFuture.isSuccess()) {
            throw new ConnectException(channelFuture.cause().getMessage());
        }
    }

    public void shutdown() {
        eventExecutors.shutdownGracefully();
    }

    @Override
    public void sendPacket(PayloadPacket payloadPacket) {
        if (payloadPacket == null) {
            throw new IllegalArgumentException("Packet is null");
        }
        channelFuture.channel().writeAndFlush(payloadPacket);
        log.debug("Sent packet {}", payloadPacket.getClass().getSimpleName());
    }

    @Override
    public PayloadPacketHandler packetHandler() {
        return packetHandler;
    }
}
