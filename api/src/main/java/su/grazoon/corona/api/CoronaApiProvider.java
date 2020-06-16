package su.grazoon.corona.api;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author glowgrew
 */
public class CoronaApiProvider {

    private static CoronaApi coronaApi;

    public static CoronaApi get() {
        return checkNotNull(coronaApi, "Corona API is not loaded yet");
    }

    public static void setInstance(CoronaApi instance) {
        coronaApi = instance;
    }
}
