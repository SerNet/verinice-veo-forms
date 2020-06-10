import java.util.Calendar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    groovy
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
    id("com.diffplug.gradle.spotless") version "4.0.1"
    id("com.github.hierynomus.license") version "0.15.0"
    jacoco
}

group = "org.veo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-test")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-ui:1.3.9")
    implementation("io.mockk:mockk:1.10.0")

    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("org.spockframework:spock-spring:1.3-groovy-2.5")
    testImplementation("com.h2database:h2:1.4.199")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.register("formatApply") {
    dependsOn("spotlessApply")
    dependsOn("licenseFormat")
}

spotless {
    format("misc") {
        target("**/*.md", "**/*.gitignore")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    groovy {
        greclipse()
        indentWithSpaces()
        trimTrailingWhitespace()
    }
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

license {
    header = file("templates/licenseHeader.txt")
    skipExistingHeaders = true
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] = ProcessBuilder("git", "config", "user.name").start()
            .inputStream.bufferedReader().readText().trim()
}
