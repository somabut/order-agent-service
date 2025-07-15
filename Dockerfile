FROM amazoncorretto:21-alpine

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

RUN mkdir -p /loki-data \
    && mkdir -p /loki-wal \
    && mkdir -p /logs/log \
    && mkdir -p /grafana-data \
    && chmod -R 777 /loki-data /loki-wal /logs/log /grafana-data

COPY loki-config.yml loki-config.yml
COPY promtail-config.yml promtail-config.yml

ENTRYPOINT ["java", "-jar", "app.jar"]