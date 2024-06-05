FROM maven:3-jdk-11
#FROM maven:3.8.3-openjdk-16
ENV MAVEN_CLI_OPTS "--batch-mode --show-version --threads 2C"
COPY pom.xml /code/pom.xml
COPY core /code/core
COPY plugin /code/plugin
WORKDIR /code/
RUN mvn $MAVEN_CLI_OPTS install package -DskipTests
RUN mkdir -p /app/core && mkdir -p /app/plugin/jar \
 && mv /code/core/target/*.jar /app/core/ \
 && mv /code/plugin/jar/*.jar /app/plugin/jar/

# RUN rm -r /code
COPY conf/sts_metadata.test.xml /app/conf/sts_metadata.test.xml

# For testing
COPY plugin/cpr/data/keyfile.json /tmp/test_cpr_key.json
EXPOSE 8000
WORKDIR /app/core

CMD ["java", "-Dloader.path=/app/plugin/jar/", "-cp", "datafordeler-core-exec.jar", "org.springframework.boot.loader.PropertiesLauncher"]
