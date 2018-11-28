package com.tomnocon.keycloak.audit;

import org.fluentd.logger.FluentLogger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

public class FluentdEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(FluentdEventListenerProviderFactory.class);
    private static final String ID = "fluentd";

    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new FluentdEventListenerProvider();
    }

    public void init(Config.Scope scope) {

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
