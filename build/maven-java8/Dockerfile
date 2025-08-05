# Create runtime image
FROM egovio/8-openjdk-alpine
# FROM egovio/amazoncorretto:17-alpine3.19

WORKDIR /opt/egov

ARG JAR_FILE

COPY ${JAR_FILE} app.jar

COPY build/maven/start.sh start.sh

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]