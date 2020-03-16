package su.grazoon.corona.common.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ServerProfile {

    private final ServerGroup group;
    private final String name;

    public ServerProfile(ServerGroup group, String name) {
        checkArgument(group.matches(name), "Server name (%s) does not match the server group (%s)", name, group);

        this.group = checkNotNull(group);
        this.name = checkNotNull(name);
    }

    public ServerGroup getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }
}
