package su.grazoon.corona.common;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.PayloadPacket;
import su.grazoon.corona.api.PayloadPacketHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class PayloadPacketHandlerImpl implements PayloadPacketHandler {

    private static final Logger log = LoggerFactory.getLogger(PayloadPacketHandlerImpl.class);

    private final Multimap<Class<? extends PayloadPacket>, Consumer<Object>> packetHandlers;

    public PayloadPacketHandlerImpl() {
        this.packetHandlers = Multimaps.newMultimap(new HashMap<>(), HashSet::new);
    }

    @Override
    public void acceptPacket(PayloadPacket payloadPacket) {
        log.debug("Processing packet {}", payloadPacket.getClass().getSimpleName());
        Collection<Consumer<Object>> handlers = packetHandlers.get(payloadPacket.getClass());
        handlers.forEach(handler -> handler.accept(payloadPacket));
    }

    @Override
    public <T extends PayloadPacket> void registerHandler(Class<T> type, Consumer<T> handler) {
        packetHandlers.put(type, a -> handler.accept((T) a));
    }
}
