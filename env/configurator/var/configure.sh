#!/bin/sh

set -e

ELASTICSEARCH_URL=${ELASTICSEARCH_URL:=http://elasticsearch:9200}
KIBANA_URL=${KIBANA_URL:=http://kibana:5601}

dockerize -wait $ELASTICSEARCH_URL -wait $KIBANA_URL/api/status -timeout 120s

echo "> Importing GeoIP pipeline..."
curl --fail -i -o - -X PUT -H "Content-Type: application/json" $ELASTICSEARCH_URL/_ingest/pipeline/geoip --upload-file /var/geoip-pipeline.json

echo
echo "> Creating elasticsearch index..."
curl --fail -i -o - -X PUT -H "Content-Type: application/json" $ELASTICSEARCH_URL/audit --upload-file /var/audit-mappings.json

echo
echo "> Importing saved objects..."
curl --fail -i -o - -X POST -H "Content-Type: application/json" -H "kbn-xsrf: kibana" $KIBANA_URL/api/saved_objects/_bulk_create --upload-file /var/saved-objects.json