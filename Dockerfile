FROM node:26-alpine AS build-ui
WORKDIR /ui

COPY ui ./
RUN --mount=type=cache,target=/root/.npm npm ci

RUN npm run build
RUN npm run check

FROM eclipse-temurin:25-alpine AS build-server
WORKDIR /app

# needed by the Kotlin wrapper to download the Toolchain distribution
RUN apk add --no-cache curl

# use the JDK from the image instead of the provisioned glibc JRE (Alpine uses musl)
ENV KOTLIN_CLI_JAVA_HOME=$JAVA_HOME
ENV KOTLIN_CLI_NO_WELCOME_BANNER=1

COPY . ./
RUN --mount=type=cache,target=/root/.cache/JetBrains/Kotlin ./kotlin build && ./kotlin do jar

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

ENV TZ=Europe/London
ENV JAVA_TOOL_OPTIONS="-Xss256K -XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError"

# host.docker.internal works locally via compose extra_hosts; CI uses host network + localhost
ARG AOT_DB_HOST=host.docker.internal
RUN ENV=prod DB_URL=jdbc:postgresql://$AOT_DB_HOST:5644/user java -XX:AOTCacheOutput=aot.cache -jar *.jar || true

USER user
CMD java -XX:AOTCache=aot.cache -jar *.jar

ENV PORT=8080
EXPOSE $PORT
