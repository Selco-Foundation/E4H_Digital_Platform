# syntax=docker/dockerfile:1.4
FROM maven:3.8.4-openjdk-17-slim AS build
ARG WORK_DIR
WORKDIR /app

# copy the project files
COPY --link ${WORK_DIR}/pom.xml ./pom.xml
COPY --link build/maven/start.sh ./start.sh

# not useful for stateless builds
# RUN mvn -B dependency:go-offline
# RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline #Downloads all dependencies

COPY --link ${WORK_DIR}/src ./src
RUN mvn -B -f /app/pom.xml package
# RUN --mount=type=cache,target=/root/.m2 mvn -B package


# Create runtime image
FROM openjdk:17-slim AS runtime


WORKDIR /opt/egov

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
