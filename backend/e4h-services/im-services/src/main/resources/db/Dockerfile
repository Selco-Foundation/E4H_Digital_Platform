FROM egovio/flyway:10.7.1

ARG JAR_FILE

COPY ${JAR_FILE} /tmp/im-services.jar

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    rm -rf /var/lib/apt/lists/*

# Unpack only the migration classes into /flyway/jars
RUN mkdir -p /flyway/jars/db/migration  && \
    jar xf /tmp/im-services.jar BOOT-INF/classes/db/migration && \
    cp -r BOOT-INF/classes/db/migration /flyway/jars/db/migration && \
    rm -rf BOOT-INF /tmp/im-services.jar

COPY ./migration/main /flyway/sql

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]
