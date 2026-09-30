FROM node:26-alpine AS build-ui
WORKDIR /ui

COPY ui ./
RUN --mount=type=cache,target=/root/.npm npm ci

RUN npm run build
RUN npm run check

FROM eclipse-temurin:25-alpine AS build-server
WORKDIR /app

COPY . ./
RUN --mount=type=cache,target=/root/.gradle ./gradlew testClasses jar --info

# The final image
FROM eclipse-temurin:25-jre-alpine AS final
RUN adduser -S user
RUN rm -fr /usr/sbin /bin/ch*

WORKDIR /app
COPY .env ./
COPY --from=build-ui /ui/build ui/public
COPY --from=build-server /app/build/libs ./

ARG VERSION=dev
ENV VERSION=$VERSION
RUN echo "Setting built version to $VERSION" && sed -Ei "s/\\\$VERSION/$VERSION/" ui/public/index.html
RUN gzip -k9 ui/public/assets/*

USER user

ENV TZ=Europe/London
ENV JAVA_TOOL_OPTIONS="-Xss256K -XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError"

RUN ENV=prod DB_URL=jdbc:postgresql://host.docker.internal:5644/user java -XX:AOTCacheOutput=/tmp/aot.cache -jar *.jar || true
CMD java -XX:AOTCache=/tmp/aot.cache -jar *.jar

ENV PORT=8080
EXPOSE $PORT
