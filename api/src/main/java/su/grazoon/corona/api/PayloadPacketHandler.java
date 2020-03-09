package su.grazoon.corona.api;

import java.util.function.Consumer;

/**
 * @author glowgrew
 */
public interface PayloadPacketHandler {

    void acceptPacket(PayloadPacket payloadPacket);

    <T extends PayloadPacket> void registerHandler(Class<T> type, Consumer<T> handler);
}
