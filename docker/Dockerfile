FROM jelastic/maven:3.9.9-openjdk-21.0.2-almalinux-9 AS dependencies

WORKDIR /build/

ENV MAVEN_CLI_OPTS "--batch-mode --show-version --threads 2C"

RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=bind,source=parent/pom.xml,target=parent/pom.xml \
    --mount=type=bind,source=core/pom.xml,target=core/pom.xml \
    --mount=type=bind,source=plugin/adresseservice/pom.xml,target=plugin/adresseservice/pom.xml \
    --mount=type=bind,source=plugin/combinedPitu/pom.xml,target=plugin/combinedPitu/pom.xml \
    --mount=type=bind,source=plugin/cpr/pom.xml,target=plugin/cpr/pom.xml \
    --mount=type=bind,source=plugin/cvr/pom.xml,target=plugin/cvr/pom.xml \
    --mount=type=bind,source=plugin/eboks/pom.xml,target=plugin/eboks/pom.xml \
    --mount=type=bind,source=plugin/eskat/pom.xml,target=plugin/eskat/pom.xml \
    --mount=type=bind,source=plugin/geo/pom.xml,target=plugin/geo/pom.xml \
    --mount=type=bind,source=plugin/ger/pom.xml,target=plugin/ger/pom.xml \
    --mount=type=bind,source=plugin/prisme/pom.xml,target=plugin/prisme/pom.xml \
    --mount=type=bind,source=plugin/statistik/pom.xml,target=plugin/statistik/pom.xml \
    --mount=type=bind,source=plugin/subscription/pom.xml,target=plugin/subscription/pom.xml \
    mvn dependency:go-offline -DskipTests


FROM dependencies AS package

COPY core /build/core
COPY plugin /build/plugin
WORKDIR /build

RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=bind,source=parent/pom.xml,target=parent/pom.xml \
    --mount=type=bind,source=core/pom.xml,target=core/pom.xml \
    --mount=type=bind,source=plugin/adresseservice/pom.xml,target=plugin/adresseservice/pom.xml \
    --mount=type=bind,source=plugin/combinedPitu/pom.xml,target=plugin/combinedPitu/pom.xml \
    --mount=type=bind,source=plugin/cpr/pom.xml,target=plugin/cpr/pom.xml \
    --mount=type=bind,source=plugin/cvr/pom.xml,target=plugin/cvr/pom.xml \
    --mount=type=bind,source=plugin/eboks/pom.xml,target=plugin/eboks/pom.xml \
    --mount=type=bind,source=plugin/eskat/pom.xml,target=plugin/eskat/pom.xml \
    --mount=type=bind,source=plugin/geo/pom.xml,target=plugin/geo/pom.xml \
    --mount=type=bind,source=plugin/ger/pom.xml,target=plugin/ger/pom.xml \
    --mount=type=bind,source=plugin/prisme/pom.xml,target=plugin/prisme/pom.xml \
    --mount=type=bind,source=plugin/statistik/pom.xml,target=plugin/statistik/pom.xml \
    --mount=type=bind,source=plugin/subscription/pom.xml,target=plugin/subscription/pom.xml \
    mvn package -DskipTests


FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /app

COPY --from=package /build/core/target/*.jar /app/core/
COPY --from=package /build/plugin/jar/*.jar /app/plugin/

ARG COMMIT_TAG
ENV COMMIT_TAG=${COMMIT_TAG:-HEAD}

# For testing
COPY conf/sts_metadata.test.xml /app/conf/sts_metadata.test.xml
COPY plugin/cpr/data/keyfile.json /tmp/test_cpr_key.json
COPY --chmod=0500 docker/entrypoint.sh /app/entrypoint.sh
#COPY docker/entrypoint.sh /app/entrypoint.sh


EXPOSE 8000
WORKDIR /app

ENTRYPOINT ["/app/entrypoint.sh"]
#ENTRYPOINT ["java", "-Dloader.path=/app/plugin/", "-cp", "/app/core/datafordeler-core-exec.jar", "org.springframework.boot.loader.launch.JarLauncher"]
CMD ["java", "-Dloader.path=/app/plugin/", "-cp", "/app/core/datafordeler-core-exec.jar", "org.springframework.boot.loader.launch.PropertiesLauncher"]
