package su.grazoon.corona.common.server;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum ServerGroup {

    // SYSTEM SERVERS
    PROXY("proxy"), // proxy server
    AUTH("auth"), // auth server
    HUB("hub"), // the main server hub

    // GAME SERVERS
    SURVIVAL("survival"), // game lobby
    BED_WARS("bw"), // bed wars game
    SKYWARS("sw"),

    UNKNOWN("unknown"); // skywars game

    private final String alias;

    ServerGroup(String alias) {
        this.alias = alias;
    }

    public static ServerGroup guessOf(String name) {
        return name.contains("-") && name.split("-").length == 2 ?
               Arrays.stream(values()).filter(group -> group.matches(name)).findFirst().orElse(UNKNOWN) :
               UNKNOWN;
    }

    public boolean matches(String name) {
        return name.startsWith(alias);
    }

    @NotNull
    public String getAlias() {
        return alias;
    }
}
