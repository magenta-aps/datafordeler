FROM jelastic/maven:3.9.9-openjdk-23.0.1-almalinux-9 AS package

WORKDIR /build/

COPY pom.xml pom.xml
COPY parent/pom.xml parent/pom.xml
COPY core core
COPY plugin plugin

RUN mvn package -DskipTests --batch-mode --show-version --threads 2C


FROM jelastic/maven:3.9.9-openjdk-23.0.1-almalinux-9 AS final
WORKDIR /app
ARG COMMIT_TAG
ENV COMMIT_TAG=${COMMIT_TAG:-HEAD}
RUN apk add --no-cache bash maven

COPY --from=package /build/core/target/*.jar /app/core/
COPY --from=package /build/plugin/jar/*.jar /app/plugin/
COPY --chmod=0500 docker/entrypoint.sh /app/entrypoint.sh

# For testing
COPY conf/sts_metadata.test.xml /app/conf/sts_metadata.test.xml
COPY plugin/cpr/data/keyfile.json /tmp/test_cpr_key.json

EXPOSE 8000
ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["java", "-Dloader.path=/app/plugin/", "-cp", "/app/core/datafordeler-core-exec.jar", "org.springframework.boot.loader.launch.PropertiesLauncher"]
