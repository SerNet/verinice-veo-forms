import java.util.Calendar
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.3.71"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.4.31"
    id("com.diffplug.spotless") version "5.9.0"
    id("org.cadixdev.licenser") version "0.5.1"
    jacoco
    id("com.gorylenko.gradle-git-properties") version "2.2.4"
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
    implementation("org.springdoc:springdoc-openapi-ui:1.5.3")
    implementation("io.mockk:mockk:1.10.5")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.0")
    testImplementation("org.codehaus.groovy:groovy-json:3.0.7")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.0")
    testImplementation("io.kotest:kotest-property-jvm:4.4.0")
    testImplementation("com.h2database:h2:1.4.200")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
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
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

license {
    header = file("templates/licenseHeader.txt")
    newLine = false
    skipExistingHeaders = true
    style(closureOf<HeaderFormatRegistry> {
        put("kt", "JAVADOC")
    })
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] = ProcessBuilder("git", "config", "user.name").start()
            .inputStream.bufferedReader().readText().trim()
}

// Add no-arg ORM constructors for JPA entities.
noArg {
    annotation("javax.persistence.Entity")
}

springBoot {
    buildInfo {
        properties {
            if (getRootProject().hasProperty("ciBuildNumer")) {
                additional = mapOf(
                    "ci.buildnumber" to rootProject.properties["ciBuildNumer"],
                    "ci.jobname" to rootProject.properties["ciJobName"]
                )
            }
        }
    }
}
