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
import su.grazoon.corona.api.credentials.SenderType;
import su.grazoon.corona.common.CoronaPayloadPacketHandler;
import su.grazoon.corona.common.packet.HandshakePacket;
import su.grazoon.corona.common.server.Server;
import su.grazoon.corona.common.server.ServerGroup;
import su.grazoon.corona.common.server.ServerProfile;

import static com.google.common.base.Preconditions.checkNotNull;

public class NativeNettyClient implements NettyClient {

    private static final Logger log = LoggerFactory.getLogger(NativeNettyClient.class);

    private final PayloadPacketHandler packetHandler;

    private final EventLoopGroup eventExecutors;
    private final Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    private SenderType senderType;

    public NativeNettyClient(int threadAmount) {
        packetHandler = new CoronaPayloadPacketHandler();

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
                         channelPipeline.addLast("object_decoder",
                                                 new ObjectDecoder(ClassResolvers.weakCachingResolver(getClass().getClassLoader())));
                         channelPipeline.addLast("client_handler", new CoronaClientPacketHandler(packetHandler));
                     }
                 });

        senderType = SenderType.UNKNOWN;
    }

    @Override
    public void connect(ConnectionCredentials credentials) {
        channelFuture = bootstrap.connect(credentials.getHostname(), credentials.getPort());
        try {
            channelFuture.sync();
            log.info("Connected to Corona ({})", credentials.getFormattedAddress());
            senderType = credentials.getSenderType();
            ServerProfile profile = new ServerProfile(ServerGroup.guessOf(credentials.getServer()),
                                                      credentials.getServer());
            sendPacket(new HandshakePacket(senderType, new Server(profile, 0, 100)));
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread was interrupted");
        }
        if (!channelFuture.isSuccess()) {
            log.error("IO operation wasn't completed successfully: {}", channelFuture.cause().getMessage());
        }
    }

    @Override
    public void shutdown() {
        log.info("Closing IO threads");
        eventExecutors.shutdownGracefully();
    }

    @Override
    public ChannelFuture sendPacket(PayloadPacket payloadPacket) {
        checkNotNull(payloadPacket, "packet");

        log.debug("[{} => Corona] Sent packet {}", senderType.display(), payloadPacket.getClass().getSimpleName());
        return channelFuture.channel().writeAndFlush(payloadPacket);
    }

    @Override
    public PayloadPacketHandler packetHandler() {
        return packetHandler;
    }
}
