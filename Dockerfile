FROM eclipse-temurin:21-jre-jammy

ARG JAR_FILE=build/libs/*.jar

WORKDIR /

COPY ${JAR_FILE} app.jar

RUN mkdir -p /loki-data \
    && mkdir -p /loki-wal \
    && mkdir -p /logs/log \
    && mkdir -p /grafana-data \
    && chmod -R 777 /loki-data /loki-wal /logs/log /grafana-data

COPY loki-config.yml loki-config.yml
COPY promtail-config.yml promtail-config.yml

#COPY word_compare.py word_compare.py
#COPY comparator.py comparator.py
#
#RUN apt-get update && apt-get install -y \
#    python3 python3-venv python3-pip gcc g++ make \
#    && rm -rf /var/lib/apt/lists/*
#
#RUN python3 -m venv venv && \
#    ./venv/bin/pip install --upgrade pip && \
#    ./venv/bin/pip install jep numpy scikit-learn
#
## 3. java.library.path를 Jep의 so(네이티브 라이브러리) 위치로 지정
#ENV JAVA_OPTS="-Djava.library.path=/venv/lib/python3.11/site-packages/jep"
#
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

ENTRYPOINT ["java", "-jar", "app.jar"]
