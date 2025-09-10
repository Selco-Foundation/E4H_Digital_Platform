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
FROM openjdk:17-slim as flywaydl

RUN apt-get update && apt-get install -y wget && \
    mkdir -p /flyway/flyway-cli && \
    wget -O /flyway/flyway-cli/flyway-commandline-9.22.3.tar.gz https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/9.22.3/flyway-commandline-9.22.3-linux-x64.tar.gz && \
    tar -xzf /flyway/flyway-cli/flyway-commandline-9.22.3.tar.gz -C /flyway/flyway-cli && \
    rm /flyway/flyway-cli/flyway-commandline-9.22.3.tar.gz && \
    rm -rf /flyway/flyway-cli/flyway-9.22.3/jre /flyway/flyway-cli/flyway-9.22.3/licenses /flyway/flyway-cli/flyway-9.22.3/assets /flyway/flyway-cli/flyway-9.22.3/README.txt

FROM egovio/flyway:10.7.1

COPY --from=builder /migrations /flyway/jars
COPY --from=flywaydl /flyway/flyway-cli /flyway/flyway-cli

COPY ./migration/main /flyway/sql

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]