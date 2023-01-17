FROM openjdk:17-alpine3.14
ARG ARTIFACT_VERSION
COPY app/target/systemA-reporting-app-${ARTIFACT_VERSION}.jar /usr/local/share/applications/cotalent/systemA-reporting-app.jar
ADD docker/application.yml /usr/local/share/applications/cotalent/
LABEL reporting.version=${ARTIFACT_VERSION}
ENV REPORTING_VERSION=${ARTIFACT_VERSION}
VOLUME [ "/var/systemA-reporting/input", "/var/systemA-reporting/output", "/var/log/reporting/"]
ENTRYPOINT [ "java", "-jar", "/usr/local/share/applications/cotalent/systemA-reporting-app.jar", "--spring.config.additional-location=/usr/local/share/applications/cotalent/application.yml" ]