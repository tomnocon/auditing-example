package com.tomnocon.keycloak.audit;

@FunctionalInterface
public interface EventListener<T> {
    void onEvent(T event);
}
