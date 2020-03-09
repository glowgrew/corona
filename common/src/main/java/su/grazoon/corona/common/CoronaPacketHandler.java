package su.grazoon.corona.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;

import static io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class CoronaPacketHandler extends SimpleChannelInboundHandler<PayloadPacket> {

    private static final Logger log = LoggerFactory.getLogger(CoronaPacketHandler.class);

    private final PayloadPacketHandler packetHandler;

    public CoronaPacketHandler(PayloadPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PayloadPacket msg) {
        log.debug("Received packet {}", msg.getClass().getSimpleName());
        packetHandler.acceptPacket(msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        log.debug("Registered new channel {}", ctx.channel().id().asShortText());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
