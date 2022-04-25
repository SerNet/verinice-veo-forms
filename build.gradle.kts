import com.diffplug.spotless.FormatterStep
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Calendar

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.6.21"

    id("com.diffplug.spotless") version "6.3.0"
    id("org.cadixdev.licenser") version "0.6.1"
    jacoco
    id("com.gorylenko.gradle-git-properties") version "2.4.0"
}

group = "org.veo"
version = "0.10-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-test")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.3.4")
    implementation("com.vladmihalcea:hibernate-types-52:2.14.1")
    implementation("org.flywaydb:flyway-core:8.5.9")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    implementation("io.mockk:mockk:1.12.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("net.swiftzer.semver:semver:1.2.0")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    val kotestVersion = "5.0.3"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    val testcontainersVersion = "1.16.3"
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
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
    json {
        target("**/*.json")
        addStep(object : FormatterStep {
            override fun getName() = "format json"
            override fun format(rawUnix: String, file: File): String {
                val om = ObjectMapper()
                return om.writer()
                    .with(DefaultPrettyPrinter().apply { indentArraysWith(SYSTEM_LINEFEED_INSTANCE) })
                    .writeValueAsString(om.readValue(rawUnix, Map::class.java))
            }
        })
    }
}

license {
    header.set(resources.text.fromFile("templates/licenseHeader.txt"))
    newLine.set(false)
    skipExistingHeaders.set(true)
    exclude("**/*.properties")
    style(
        closureOf<HeaderFormatRegistry> {
            put("kt", "JAVADOC")
        }
    )
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
