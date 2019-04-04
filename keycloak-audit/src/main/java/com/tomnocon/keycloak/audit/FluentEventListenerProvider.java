package com.tomnocon.keycloak.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fluentd.logger.FluentLogger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FluentEventListenerProvider implements EventListenerProvider {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FluentLogger fluentLogger;

    private KeycloakSession keycloakSession;

    FluentEventListenerProvider(FluentLogger fluentLogger, KeycloakSession keycloakSession) {
        this.fluentLogger = fluentLogger;
        this.keycloakSession = keycloakSession;
    }

    @Override
    public void onEvent(Event event) {
        AuthDescriptor auth = new AuthDescriptor();
        auth.setRealmId(event.getRealmId());
        auth.setClientId(getClientId(event));
        auth.setUserId(event.getUserId());
        auth.setSessionId(event.getSessionId());
        auth.setIpAddress(event.getIpAddress());

        Map<String, Object> eventDetails = new HashMap<>(event.getDetails());
        String tag = getUserEventTag(event);

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setAuth(auth);
        auditEvent.setTimestamp(Instant.ofEpochMilli(event.getTime()).toString());
        auditEvent.setDetails(eventDetails);
        auditEvent.setTag(tag);

        Map<String, Object> auditEventMap = OBJECT_MAPPER.convertValue(auditEvent, new TypeReference<Map<String, Object>>() {});
        fluentLogger.log(tag, auditEventMap, TimeUnit.MILLISECONDS.toSeconds(event.getTime()));
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        AuditEvent auditEvent = new AuditEvent();

        AuthDescriptor auth = new AuthDescriptor();
        auth.setRealmId(adminEvent.getAuthDetails().getRealmId());
        auth.setClientId(adminEvent.getAuthDetails().getClientId());
        auth.setUserId(adminEvent.getAuthDetails().getUserId());
        auth.setIpAddress(adminEvent.getAuthDetails().getIpAddress());
        String sessionId = getLastUserSessionId(adminEvent.getAuthDetails().getUserId());
        if (sessionId != null) {
            auth.setSessionId(sessionId);
        }

        try {
            Map<String, Object> eventDetails = OBJECT_MAPPER.readValue(adminEvent.getRepresentation(), new TypeReference<Map<String, Object>>() {
            });
            auditEvent.setDetails(eventDetails);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String tag = getAdminEventTag(adminEvent);

        auditEvent.setAuth(auth);
        auditEvent.setTag(tag);
        auditEvent.setTimestamp(Instant.ofEpochMilli(adminEvent.getTime()).toString());

        Map<String, Object> auditEventMap = OBJECT_MAPPER.convertValue(auditEvent, new TypeReference<Map<String, Object>>() {});
        fluentLogger.log(tag, auditEventMap, TimeUnit.MILLISECONDS.toSeconds(adminEvent.getTime()));
    }

    @Override
    public void close() {

    }

    private String getUserEventTag(Event event) {
        EventType eventType = event.getType();
        String tag = "keycloak.user_event";
        if (eventType != null) {
            String eventTypeAsString = eventType.toString();
            if (eventTypeAsString != null) {
                tag += "." + eventTypeAsString.toLowerCase();
            }
        }
        return tag;
    }

    private String getAdminEventTag(AdminEvent adminEvent) {
        String resourceName = null, operationName = null;
        ResourceType resourceType = adminEvent.getResourceType();
        OperationType operationType = adminEvent.getOperationType();
        if (resourceType != null) {
            resourceName = resourceType.name();
        }
        if (operationType != null) {
            operationName = operationType.name();
        }
        String tag = "keycloak.admin_event";
        if (resourceName != null) {
            tag += "." + resourceName.toLowerCase();
            if (operationName != null) {
                tag += "." + operationName.toLowerCase();
            } else {
                tag += ".*";
            }
        }
        return tag;
    }

    private String getLastUserSessionId(String userId) {
        RealmModel realmModel = keycloakSession.getContext().getRealm();
        UserModel userModel = keycloakSession.users().getUserById(userId, realmModel);

        List<UserSessionModel> userSessions = keycloakSession.sessions().getUserSessions(realmModel, userModel);
        if (userSessions.size() > 0) {
            return userSessions.get(userSessions.size() - 1).getId();
        }
        return null;
    }

    private String getClientId(Event event) {
        String clientId = event.getClientId();
        if (clientId != null) {
            RealmModel realmModel = keycloakSession.getContext().getRealm();
            ClientModel clientModel = realmModel.getClientByClientId(clientId);
            if (clientModel != null) {
                return clientModel.getId();
            }
            return clientId;
        }
        return null;
    }
}
