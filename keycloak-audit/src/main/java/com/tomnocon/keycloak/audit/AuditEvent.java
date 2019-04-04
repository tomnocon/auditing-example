package com.tomnocon.keycloak.audit;

import lombok.Data;

import java.util.Map;

@Data
public class AuditEvent {
    private String timestamp;
    private AuthDescriptor auth;
    private Map<String, Object> details;
    private String tag;
}
