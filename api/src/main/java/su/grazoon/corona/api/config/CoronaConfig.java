package su.grazoon.corona.api.config;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CoronaConfig {

    Config getHandle();

    @Nullable
    default String getString(@NotNull String path) {
        return getString(path, null);
    }

    String getString(@NotNull String path, String def);

    default int getInt(@NotNull String path) {
        return getInt(path, 0);
    }

    int getInt(@NotNull String path, int def);
}

