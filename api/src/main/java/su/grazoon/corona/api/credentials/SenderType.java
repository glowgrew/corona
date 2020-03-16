package su.grazoon.corona.api.credentials;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a packet sender type
 *
 * @author glowgrew
 */
public enum SenderType {

    CORONA, // Corona server
    PAPER, // bukkit client
    VELOCITY, // proxy client
    UNKNOWN; // not specified

    /**
     * Capitalizes the first letter of an enumeration
     *
     * @return formatted enum name
     */
    public String display() {
        StringBuilder displayType = new StringBuilder();
        String[] type = name().split("_");
        for (String t : type) {
            String tmp = StringUtils.capitalize(t.toLowerCase());
            displayType.append(tmp).append(" ");
        }
        return displayType.toString().trim(); // trim extra space chars
    }
}
