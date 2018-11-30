package com.tomnocon.keycloak.audit;

import org.keycloak.Config;

class FluentConfig {

    private final static String TAG_PREFIX  = "tag-prefix";
    private final static String HOST  = "host";
    private final static String PORT  = "port";

    private final String tagPrefix;
    private final String host;
    private final int port;

    private FluentConfig(String tagPrefix, String host, int port) {
        this.tagPrefix = tagPrefix;
        this.host = host;
        this.port = port;
    }

    String getTagPrefix() {
        return tagPrefix;
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    static FluentConfig fromScopeConfig(Config.Scope scope) {
        String tagPrefix = scope.get(TAG_PREFIX, "audit");
        String host = scope.get(HOST, "localhost");
        int port = scope.getInt(PORT, 24224);
        return new FluentConfig(tagPrefix, host, port);
    }
}
