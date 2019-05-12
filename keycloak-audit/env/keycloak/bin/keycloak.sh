#!/usr/bin/env bash

set -eu
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/keycloak.cli

exec /opt/jboss/tools/docker-entrypoint.sh -c standalone.xml "$@"
exit "$?"