package com.tomnocon.keycloak.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fluentd.logger.FluentLogger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FluentEventListenerProvider implements EventListenerProvider {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FluentLogger fluentLogger;

    FluentEventListenerProvider(FluentLogger fluentLogger) {
        this.fluentLogger = fluentLogger;
    }

    @Override
    public void onEvent(Event event) {
        Map<String, Object> map = OBJECT_MAPPER.convertValue(event, new TypeReference<Map<String, Object>>(){});
        fluentLogger.log(getUserEventTag(event), map, TimeUnit.MILLISECONDS.toSeconds(event.getTime()));
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {

    }

    @Override
    public void close() {

    }

    private String getUserEventTag(Event event) {
        EventType eventType = event.getType();
        String tag = "user_event";
        if (eventType != null) {
            String eventTypeAsString = eventType.toString();
            if (eventTypeAsString != null) {
                tag += "." + eventTypeAsString.toLowerCase();
            }
        }
        return tag;
    }
}
