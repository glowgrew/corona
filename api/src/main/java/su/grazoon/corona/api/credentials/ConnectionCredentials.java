package su.grazoon.corona.api.credentials;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a connection data used by client or server.
 *
 * @author glowgrew
 */
public interface ConnectionCredentials {

    @NotNull
    String getFormattedAddress();

    @NotNull
    String getHostname();

    int getPort();

    @NotNull
    String getServer();

    @NotNull
    SenderType getSenderType();
}
