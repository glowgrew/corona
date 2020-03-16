package su.grazoon.corona.common.server;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class Proxy {

    private int onlinePlayers, maxPlayers;
    private List<String> tabHeader, tabFooter;

    public Proxy(int onlinePlayers, int maxPlayers, List<String> tabHeader, List<String> tabFooter) {
        checkArgument(onlinePlayers < 0, "Current server online cannot be lower than 0");
        checkArgument(maxPlayers < 0, "Maximum server player limit cannot be lower than 0");

        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.tabHeader = tabHeader;
        this.tabFooter = tabFooter;
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

    public List<String> getTabHeader() {
        return tabHeader;
    }

    public void setTabHeader(List<String> tabHeader) {
        this.tabHeader = tabHeader;
    }

    public List<String> getTabFooter() {
        return tabFooter;
    }

    public void setTabFooter(List<String> tabFooter) {
        this.tabFooter = tabFooter;
    }
}
