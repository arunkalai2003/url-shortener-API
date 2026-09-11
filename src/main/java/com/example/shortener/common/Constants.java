package com.example.shortener.common;

public class Constants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * This class is intended to be used only for its {@code static} members and
     * should never be instantiated — not even via reflection. Any attempt to do
     * so results in an {@link IllegalStateException}.
     *
     * @throws IllegalStateException always, to signal that instantiation is not permitted
     */
    private Constants() {
        throw new IllegalStateException("No instances of " + getClass().getName() + " are allowed");
    }

    /**
     * list of all HTTP header names as constants
     */
    public static final String X_COUNTRY_HEADER_NAME = "X-Country";
    public static final String X_REGION_HEADER_NAME = "X-Region";
    public static final String REFERER_HEADER_NAME = "Referer";
    public static final String USER_AGENT_HEADER_NAME = "User-Agent";

    public static final String UNKNOWN = "UNKNOWN";
}
