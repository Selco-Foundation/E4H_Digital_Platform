#!/bin/sh

CLASSPATH="/flyway/jars"
for jar in /flyway/jars/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done
CLASSPATH="$CLASSPATH:/flyway/flyway-cli/flyway-9.22.3/lib/*:/flyway/flyway-cli/flyway-9.22.3/lib/community/*"

exec java -cp "$CLASSPATH" org.flywaydb.commandline.Main \
  -url=$DB_URL \
  -table=$SCHEMA_TABLE \
  -locations=$FLYWAY_LOCATIONS \
  -jarDirs=/flyway/jars \
  -baselineOnMigrate=true \
  -outOfOrder=true \
  migrate