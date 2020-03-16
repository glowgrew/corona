package su.grazoon.corona.common.server;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Server {

    @NotNull
    private ServerProfile profile;

    private int onlinePlayers, maxPlayers;

    public Server(ServerProfile profile, int onlinePlayers, int maxPlayers) {
        checkArgument(onlinePlayers < 0, "Current server online cannot be lower than 0");
        checkArgument(maxPlayers < 0, "Maximum server player limit cannot be lower than 0");

        this.profile = checkNotNull(profile);
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    @NotNull
    public ServerProfile getProfile() {
        return profile;
    }

    public void setProfile(@NotNull ServerProfile profile) {
        this.profile = profile;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
