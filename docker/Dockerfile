FROM maven:3-jdk-11
#FROM maven:3.8.3-openjdk-16
ENV MAVEN_CLI_OPTS "--batch-mode --show-version --threads 2C"
COPY pom.xml /code/pom.xml
COPY core /code/core
COPY plugin /code/plugin
WORKDIR /code/
RUN mvn $MAVEN_CLI_OPTS install package -DskipTests
RUN mkdir -p /dist/core && mkdir -p /dist/plugin/jar \
    &&  mv /code/core/target/*.jar /dist/core/ \
    && mv /code/plugin/jar/*.jar /dist/plugin/jar/
#COPY docker/hibernate.xml /configs/hibernate.xml
COPY docker/dev-settings.properties /dist/core/application.properties

WORKDIR /dist/core
CMD ["java", "-Dloader.path=/dist/plugin/jar/", "-cp", "datafordeler-core-exec.jar", "org.springframework.boot.loader.PropertiesLauncher"]

#https://git.magenta.dk/osflow/osflow/-/blob/develop/backend/docker/Dockerfile
