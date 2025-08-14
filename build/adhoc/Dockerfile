FROM openjdk:17-slim

WORKDIR /opt/egov

ARG JAR_FILE

COPY ${JAR_FILE} app.jar

COPY build/maven/start.sh start.sh

# Install required packages and ffmpeg
RUN set -e && \
    apt update && \
    apt install -y --no-install-recommends software-properties-common wget cpulimit xz-utils && \
    wget -O ffmpeg.tar.xz https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-linux64-gpl.tar.xz && \
    tar -xf ffmpeg.tar.xz && \
    cp ffmpeg-master-latest-linux64-gpl/bin/ffmpeg /usr/local/bin/ && \
    cp ffmpeg-master-latest-linux64-gpl/bin/ffprobe /usr/local/bin/ && \
    ln -sf /usr/local/bin/ffmpeg /usr/bin/ffmpeg && \
    ln -sf /usr/local/bin/ffprobe /usr/bin/ffprobe && \
    rm -rf ffmpeg-master-latest-linux64-gpl ffmpeg.tar.xz /var/lib/apt/lists/*

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
