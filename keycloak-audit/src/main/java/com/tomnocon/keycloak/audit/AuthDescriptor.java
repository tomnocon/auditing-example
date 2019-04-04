package com.tomnocon.keycloak.audit;

import lombok.Data;

@Data
public class AuthDescriptor {
    private String realmId;
    private String clientId;
    private String userId;
    private String sessionId;
    private String ipAddress;
}
