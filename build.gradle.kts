import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
}

group = "de.amplimind.codingchallenge"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

val commonSpringBootVersion: String by extra
val coroutinesVersion: String by extra
val retrofit2Version: String by extra
val mockKVersion: String by extra
val jjwtVersion: String by extra
val postgresVersion: String by extra

repositories {
    mavenCentral()
}

dependencies {
    api("io.jsonwebtoken:jjwt-api:0.12.5")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.session:spring-session-core:$commonSpringBootVersion")
    implementation("org.springframework.session:spring-session-data-redis:$commonSpringBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$commonSpringBootVersion")
    implementation("org.springframework.data:spring-data-redis:$commonSpringBootVersion")
    implementation("org.springframework.boot:spring-boot-docker-compose:$commonSpringBootVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.0-RC1")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.postgresql:postgresql:$postgresVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofit2Version")
    implementation("com.squareup.retrofit2:retrofit-mock:$retrofit2Version")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofit2Version")
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")

    implementation("commons-validator:commons-validator:1.8.0")

    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springdoc:springdoc-openapi-kotlin:1.8.0")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-orgjson:$jjwtVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:$mockKVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
