plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("kapt") version "2.0.21"
    kotlin("plugin.jpa") version "1.9.25"
    id("jacoco")
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.7.22"
}
val springAiVersion by extra("1.0.0")
val springCloudVersion by extra("2025.0.0")

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")

    //neo4j
    implementation("org.neo4j.driver:neo4j-java-driver:5.23.0")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")

    //coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    //aws
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

    //jep
    implementation("black.ninia:jep:4.2.0")

    //logging
    implementation ("com.github.loki4j:loki-logback-appender:1.5.1")
    implementation ("net.logstash.logback:logstash-logback-encoder:7.4")

    //feign client
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    //metric
    implementation ("org.springframework.boot:spring-boot-starter-actuator")
    implementation ("io.micrometer:micrometer-registry-prometheus")

    implementation ("org.springframework.boot:spring-boot-starter:3.5.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("io.mockk:mockk-agent-jvm:1.13.11")

    //junit5
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)

//    include("**/unit/**")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.orderagentservice.OrderAgentServiceApplication.kt"
        )
    }
}

tasks.test {
    jvmArgs = listOf(
        "-Djava.library.path=C:\\Users\\hachi\\AppData\\Local\\Programs\\Python\\Python312\\Lib\\site-packages\\jep"
    )
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    requiresUnpack("**/jep-*.jar")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.test)

    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true)
    }

    val mainSrc = "${project.projectDir}/src/main/kotlin"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(
        files(
            layout.buildDirectory.dir("classes/kotlin/main").map {
                fileTree(it) {
                    exclude(
                        "**/model/**",
                        "**/exception/**"
                    )
                }
            }
        )
    )
    executionData.setFrom(layout.buildDirectory.file("jacoco/test.exec"))
}