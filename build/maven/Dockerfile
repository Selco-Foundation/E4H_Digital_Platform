FROM eclipse-temurin:17-jre-alpine

WORKDIR /opt/egov

ARG JAR_FILE

COPY ${JAR_FILE} app.jar

COPY build/maven/start.sh start.sh

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
