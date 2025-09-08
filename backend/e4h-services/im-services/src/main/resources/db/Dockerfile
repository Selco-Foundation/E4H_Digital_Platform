FROM openjdk:17-slim as builder

ARG JAR_FILE

COPY ${JAR_FILE} /tmp/im-services.jar

# Unpack migration classes and dependencies
RUN mkdir -p /migrations && \
    cd /migrations && \
    jar xf /tmp/im-services.jar BOOT-INF/classes BOOT-INF/lib && \
    cp -r BOOT-INF/classes/* . && \
    cp BOOT-INF/lib/*.jar . && \
    rm -rf BOOT-INF /tmp/im-services.jar

#flyway
FROM egovio/flyway:10.7.1

COPY --from=builder /migrations /flyway/jars

COPY ./migration/main /flyway/sql

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]