package su.grazoon.corona.common.server;

import org.jetbrains.annotations.NotNull;

public enum ServerGroup {

    HUB("hub"), // the main server hub
    LOBBY("lobby"), // game lobby
    BEDWARS("bw"), // bed wars game
    SKYWARS("sw"); // skywars game

    private final String alias;

    ServerGroup(String alias) {
        this.alias = alias;
    }

    public boolean matches(String name) {
        return name.startsWith(alias);
    }

    @NotNull
    public String getAlias() {
        return alias;
    }
}
