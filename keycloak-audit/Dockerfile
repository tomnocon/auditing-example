FROM jboss/keycloak:4.6.0.Final

COPY env/keycloak/scripts /scripts
COPY target/keycloak/providers /opt/jboss/keycloak/standalone/deployments

ENTRYPOINT ["/scripts/keycloak.sh"]