package su.grazoon.corona.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.grazoon.corona.api.config.CoronaConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author glowgrew
 */
public class DefaultCoronaConfig implements CoronaConfig {

    private static final Logger log = LoggerFactory.getLogger(DefaultCoronaConfig.class);

    private final Path path;
    private Config handle;

    public DefaultCoronaConfig(Path dataFolder, String fileName, boolean isResource) {
        boolean notExistsDataFolder = Files.notExists(dataFolder);
        if (notExistsDataFolder) {
            try {
                Files.createDirectory(dataFolder);
            } catch (IOException e) {
                throw new RuntimeException("Exception due to create directory", e);
            }
        }
        path = dataFolder.resolve(fileName);
        if (notExistsDataFolder || Files.notExists(dataFolder)) {
            if (isResource) {
                try {
                    Files.copy(getClass().getResourceAsStream("/config.conf"), path);
                } catch (IOException e) {
                    log.warn("Exception due to copy resource", e);
                }
            } else {
                try {
                    Files.createFile(dataFolder);
                } catch (IOException e) {
                    log.warn("Exception due to create file", e);
                }
            }
        }
        loadFile();
    }

    private void loadFile() {
        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            handle = ConfigFactory.parseReader(bufferedReader).resolve();
        } catch (IOException e) {
            log.error("Exception due to create BufferedReader", e);
        }
    }

    public Path getPath() {
        return path;
    }

    @NotNull
    @Override
    public Config getHandle() {
        return handle;
    }

    @Nullable
    @Override
    public String getString(@NotNull String path, String def) {
        try {
            return handle.getString(path);
        } catch (ConfigException.Missing e) {
            return def;
        }
    }

    @Override
    public int getInt(@NotNull String path, int def) {
        try {
            return handle.getInt(path);
        } catch (ConfigException.Missing e) {
            return def;
        }
    }
}
