package com.tomnocon.keycloak.audit;

import org.fluentd.logger.FluentLogger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class FluentEventListenerProviderFactory implements EventListenerProviderFactory {

    private FluentConfig config;
    private static final String ID = "fluent";

    public EventListenerProvider create(KeycloakSession keycloakSession) {
        FluentLogger fluentLogger = FluentLogger.getLogger(config.getTagPrefix(), config.getHost(), config.getPort());
        return new FluentEventListenerProvider(fluentLogger);
    }

    public void init(Config.Scope scope) {
        config = FluentConfig.fromScopeConfig(scope);
    }

    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    public void close() {
        FluentLogger.closeAll();
    }

    public String getId() {
        return ID;
    }
}
