package com.tomnocon.keycloak.audit;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

public class FluentdEventListenerProvider implements EventListenerProvider {

    @Override
    public void onEvent(Event event) {
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {

    }

    @Override
    public void close() {
        // NOOP
    }
}
