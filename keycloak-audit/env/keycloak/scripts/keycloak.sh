#!/usr/bin/env bash

set -eu
/opt/jboss/keycloak/bin/jboss-cli.sh --file=/scripts/keycloak.cli

exec /opt/jboss/tools/docker-entrypoint.sh -c standalone.xml "$@"
exit "$?"