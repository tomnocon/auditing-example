FROM jboss/keycloak:6.0.1

COPY env/keycloak  /opt/jboss/keycloak/
COPY target/keycloak/providers /opt/jboss/keycloak/standalone/deployments/

ADD --chown=jboss \
    https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.11.0/jmx_prometheus_javaagent-0.11.0.jar \
    /opt/jboss/keycloak/bin/jmx_prometheus_javaagent.jar

ENTRYPOINT ["/opt/jboss/keycloak/bin/keycloak.sh"]