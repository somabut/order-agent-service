# 멀티스테이지 빌드를 위한 Dockerfile
# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시를 위한 설정
COPY gradle gradle
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./

# 의존성 다운로드 (캐시 레이어 분리)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN gradle build --no-daemon -x test

# Stage 2: Runtime stage
FROM openjdk:21

# 메타데이터 설정
LABEL maintainer="ALIKE Team"
LABEL description="Order Agent Service - AI-powered order management system"
LABEL version="0.0.1-SNAPSHOT"

# 보안을 위한 non-root 사용자 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 작업 디렉토리 설정
WORKDIR /app

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 애플리케이션 포트 노출
EXPOSE 8081

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown -R appuser:appuser /app

# non-root 사용자로 전환
USER appuser

# 헬스 체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 