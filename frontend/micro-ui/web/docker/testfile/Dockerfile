FROM egovio/alpine-node-builder-14:yarn AS build

RUN apk update && apk upgrade

RUN apk add --no-cache git>2.30.0

ARG WORK_DIR
ARG PUBLIC_PATH

WORKDIR /app

ENV NODE_OPTIONS "--max-old-space-size=8192"

ENV GENERATE_SOURCEMAP "false"

ENV PUBLIC_PATH=${PUBLIC_PATH}

# Remove GA_MEASUREMENT_ID from build - will be injected at runtime
ENV REACT_APP_GA_ID=GA_MEASUREMENT_ID_PLACEHOLDER

COPY ${WORK_DIR} .

RUN cd web/ \
    && chmod +x install-deps.sh \
    &&  ./install-deps.sh \
    && yarn install \
    && yarn build:webpack

FROM nginx:mainline-alpine

#FROM ghcr.io/egovernments/nginx:mainline-alpine

ENV WORK_DIR=/var/web/digit-ui

RUN mkdir -p ${WORK_DIR}

COPY --from=build /app/web/build ${WORK_DIR}/

COPY --from=build /app/web/docker/nginx.conf /etc/nginx/conf.d/default.conf

COPY frontend/micro-ui/web/public/pwa-assets /var/web/digit-ui/

# Copy the runtime config injection script
COPY frontendcro-ui/web/docker/inject-config.sh /docker-entrypoint.d/inject-config.sh
RUN chmod +x /docker-entrypoint.d/inject-config.sh