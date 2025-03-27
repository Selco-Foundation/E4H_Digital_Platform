FROM egovio/alpine-node-builder-14:yarn AS build

RUN apk update && apk upgrade

RUN apk add --no-cache git>2.30.0

RUN yarn add @egovernments/digit-ui-libraries
RUN yarn add @egovernments/digit-ui-module-core
RUN yarn add @egovernments/digit-ui-module-common
RUN yarn add @selco/digit-ui-module-hrms
RUN yarn add @selco/digit-ui-module-pgr
RUN yarn add @selco/digit-ui-module-dss


ARG WORK_DIR
ARG PUBLIC_PATH

WORKDIR /app

ENV NODE_OPTIONS "--max-old-space-size=8192"

ENV GENERATE_SOURCEMAP "false"

ENV PUBLIC_PATH=${PUBLIC_PATH}

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

