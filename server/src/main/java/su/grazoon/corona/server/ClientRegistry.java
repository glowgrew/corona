package su.grazoon.corona.server;

import io.netty.channel.Channel;
import su.grazoon.corona.api.credentials.SenderType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author glowgrew
 */
public class ClientRegistry {

    private final Map<SenderType, Channel> clients;

    public ClientRegistry() {
        this.clients = new HashMap<>();
    }

    public Map<SenderType, Channel> getClients() {
        return clients;
    }

    public void registerClient(SenderType type, Channel channel) {
        clients.put(type, channel);
    }
}
