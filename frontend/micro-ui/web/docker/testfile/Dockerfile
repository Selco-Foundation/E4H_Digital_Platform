FROM egovio/alpine-node-builder-14:yarn AS build

RUN apk update && apk upgrade

RUN apk add --no-cache git>2.30.0

ARG WORK_DIR
ARG PUBLIC_PATH
ARG GA_MEASUREMENT_ID

WORKDIR /app

ENV NODE_OPTIONS "--max-old-space-size=8192"

ENV GENERATE_SOURCEMAP "false"

ENV PUBLIC_PATH=${PUBLIC_PATH}

ENV REACT_APP_GA_ID=${GA_MEASUREMENT_ID}

COPY ${WORK_DIR} .

RUN ls -lah

#RUN node web/envs.js

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
