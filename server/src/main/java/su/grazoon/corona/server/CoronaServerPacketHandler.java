package su.grazoon.corona.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;
import su.grazoon.corona.api.session.Session;
import su.grazoon.corona.common.packet.HandshakePacket;
import su.grazoon.corona.common.session.CoronaSession;

import java.util.concurrent.atomic.AtomicReference;

import static io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class CoronaServerPacketHandler extends SimpleChannelInboundHandler<PayloadPacket> {

    private static final Logger log = LoggerFactory.getLogger(CoronaServerPacketHandler.class);

    private final PayloadPacketHandler packetHandler;
    private final ClientRegistry clientRegistry;
    private final AtomicReference<Session> session;

    public CoronaServerPacketHandler(PayloadPacketHandler packetHandler, ClientRegistry clientRegistry) {
        this.packetHandler = packetHandler;
        this.clientRegistry = clientRegistry;
        this.session = new AtomicReference<>(null);
    }

    @Nullable
    public Session getSession() {
        return session.get();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Session session = new CoronaSession(ctx.channel(), packetHandler);
        this.session.compareAndSet(null, session);
        session.onReady();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        session.get().onDisconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PayloadPacket msg) {
        if (msg instanceof HandshakePacket) {
            HandshakePacket handshakePacket = (HandshakePacket) msg;
            clientRegistry.registerClient(handshakePacket.getType(), ctx.channel());
        }
        session.get().receivePacket(msg);
    }
}
