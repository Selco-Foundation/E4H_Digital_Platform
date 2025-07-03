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
    apt install -y software-properties-common wget cpulimit xz-utils && \
    wget -O ffmpeg.tar.xz https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-linux64-gpl.tar.xz && \
    tar -xf ffmpeg.tar.xz && \
    cp ffmpeg-master-latest-linux64-gpl/ffmpeg /usr/local/bin/ && \
    cp ffmpeg-master-latest-linux64-gpl/ffprobe /usr/local/bin/ && \
    rm -rf ffmpeg-master-latest-linux64-gpl ffmpeg.tar.xz /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
