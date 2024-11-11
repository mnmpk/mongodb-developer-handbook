FROM node:20 AS build-stage
WORKDIR /app
COPY package*.json /app/
RUN npm install
COPY ./ /app/
ARG CONFIG
RUN npm run build -- --output-path=./dist/out --configuration $CONFIG

FROM nginx:1.15
COPY --from=build-stage /app/dist/out/browser /usr/share/nginx/html
COPY ./nginx-custom.conf /etc/nginx/conf.d/default.conf