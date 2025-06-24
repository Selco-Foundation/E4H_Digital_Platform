FROM maven:3.8.4-openjdk-17-slim AS build
ARG WORK_DIR
WORKDIR /app

# copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh

# not useful for stateless builds
# RUN mvn -B dependency:go-offline

COPY ${WORK_DIR}/src ./src
RUN mvn -B -f /app/pom.xml package

# Create runtime image
FROM openjdk:17-slim AS runtime

WORKDIR /opt/egov

# Install required packages and ffmpeg
RUN set -e && \
    apt update && \
    apt install -y software-properties-common wget cpulimit && \
    wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz && \
    tar -xf ffmpeg-release-amd64-static.tar.xz && \
    cd ffmpeg-*-amd64-static && \
    mv ffmpeg ffprobe /usr/local/bin/ && \
    rm -rf /var/lib/apt/lists/* ffmpeg-release-amd64-static.tar.xz ffmpeg-*-amd64-static

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
