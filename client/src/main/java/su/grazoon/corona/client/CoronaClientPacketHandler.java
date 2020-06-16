package su.grazoon.corona.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;
import su.grazoon.corona.api.session.Session;
import su.grazoon.corona.common.session.CoronaSession;

import java.util.concurrent.atomic.AtomicReference;

import static io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class CoronaClientPacketHandler extends SimpleChannelInboundHandler<PayloadPacket> {

    private static final Logger log = LoggerFactory.getLogger(CoronaClientPacketHandler.class);

    private final PayloadPacketHandler packetHandler;
    private final AtomicReference<Session> session;

    public CoronaClientPacketHandler(PayloadPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
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
    protected void channelRead0(ChannelHandlerContext ctx, PayloadPacket msg) {
        session.get().receivePacket(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
