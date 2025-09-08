#!/bin/sh

CLASSPATH="/flyway/jars"
for jar in /flyway/jars/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done
CLASSPATH="$CLASSPATH:/flyway/flyway-cli/flyway-9.22.3/lib/community/*"

exec java -cp "$CLASSPATH" org.flywaydb.commandline.Main \
  -url=$DB_URL \
  -table=$SCHEMA_TABLE \
  -user=$FLYWAY_USER \
  -password=$FLYWAY_PASSWORD \
  -locations=$FLYWAY_LOCATIONS \
  -baselineOnMigrate=true \
  -outOfOrder=true \
  -X \
  migrate