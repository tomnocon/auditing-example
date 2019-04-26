FROM jboss/keycloak:5.0.0

COPY env/keycloak/scripts /scripts
COPY target/keycloak/providers /opt/jboss/keycloak/standalone/deployments

ENTRYPOINT ["/scripts/keycloak.sh"]