package su.grazoon.corona.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.NettyServer;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;
import su.grazoon.corona.api.credentials.ConnectionCredentials;
import su.grazoon.corona.common.CoronaPayloadPacketHandler;
import su.grazoon.corona.common.packet.HandshakePacket;

public class NativeNettyServer implements NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NativeNettyServer.class);


    private final ClientRegistry clientRegistry;

    private final EventLoopGroup bossGroup, workerGroup;
    private final ServerBootstrap bootstrap;
    private final PayloadPacketHandler packetHandler;

    private ChannelFuture channelFuture;

    public NativeNettyServer(int bossThreadsAmount, int workerThreadsAmount) {
        clientRegistry = new ClientRegistry();

        packetHandler = new CoronaPayloadPacketHandler();

        bossGroup = new NioEventLoopGroup(bossThreadsAmount);
        workerGroup = new NioEventLoopGroup(workerThreadsAmount);

        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .option(ChannelOption.SO_BACKLOG, 100)
                 .option(ChannelOption.SO_REUSEADDR, true)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel socketChannel) {
                         ChannelPipeline pipeline = socketChannel.pipeline();
                         pipeline.addLast("object_encoder", new ObjectEncoder());
                         pipeline.addLast("object_decoder",
                                          new ObjectDecoder(ClassResolvers.weakCachingResolver(getClass().getClassLoader())));
                         pipeline.addLast("server_handler",
                                          new CoronaServerPacketHandler(packetHandler, clientRegistry));
                     }
                 });

        packetHandler.registerHandler(HandshakePacket.class,
                                      packet -> log.info("Connected {} client", packet.getType()));
    }

    @Override
    public void bind(ConnectionCredentials credentials) {
        try {
            channelFuture = bootstrap.bind(credentials.getHostname(), credentials.getPort())
                                     .addListener(future -> log.info("CoronaApi has started on {}",
                                                                     credentials.getFormattedAddress()))
                                     .sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread is interrupted.");
        }
    }

    @Override
    public void bindAndLock(ConnectionCredentials credentials) {
        try {
            channelFuture = bootstrap.bind(credentials.getHostname(), credentials.getPort())
                                     .addListener(future -> log.info("CoronaApi has started on {}",
                                                                     credentials.getFormattedAddress()))
                                     .sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread is interrupted.");
        }
    }

    @Override
    public void shutdown() {
        log.info("CoronaApi is going to sleep...");
        log.info("Closing IO threads");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void sendPacket(PayloadPacket payloadPacket) {
        if (payloadPacket == null) {
            throw new IllegalArgumentException("Packet is null");
        }
        clientRegistry.getClients().forEach((senderType, channel) -> channel.writeAndFlush(payloadPacket));
        log.debug("[CoronaApi => Clients] Sent packet {}", payloadPacket.getClass().getSimpleName());
    }

    @Override
    public PayloadPacketHandler packetHandler() {
        return packetHandler;
    }
}
