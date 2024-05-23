FROM maven:3-jdk-11
#FROM maven:3.8.3-openjdk-16
ENV MAVEN_CLI_OPTS "--batch-mode --show-version --threads 2C"
COPY pom.xml /code/pom.xml
COPY core /code/core
COPY plugin /code/plugin
WORKDIR /code/
RUN mvn $MAVEN_CLI_OPTS clean install package -DskipTests
RUN mkdir -p /app/core && mkdir -p /app/plugin/jar && mkdir -p /app/core/conf \
 && mv /code/core/target/*.jar /app/core/ \
 && mv /code/plugin/jar/*.jar /app/plugin/jar/

# RUN rm -r /code
# COPY core/application.properties /app/core/application.properties

WORKDIR /app/core

CMD ["java", "-Dloader.path=/app/plugin/jar/", "-cp", "datafordeler-core-exec.jar", "org.springframework.boot.loader.PropertiesLauncher"]
