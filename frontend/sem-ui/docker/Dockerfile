FROM node:24-alpine AS build

RUN corepack enable

ARG WORK_DIR
WORKDIR /app

COPY ${WORK_DIR} .

ENV VITE_CONTEXT_PATH=selco-ui

RUN pnpm install --frozen-lockfile \
  && pnpm build

FROM nginx:mainline-alpine

ENV WORK_DIR=/var/web/sem-ui

RUN mkdir -p ${WORK_DIR}

COPY --from=build /app/dist ${WORK_DIR}/
COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf
