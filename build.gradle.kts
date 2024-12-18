import com.diffplug.spotless.FormatterStep
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.eclipse.jgit.api.Git
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.Calendar
import kotlin.text.Regex

plugins {
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"

    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.0"

    id("com.diffplug.spotless") version "6.25.0"
    id("org.cadixdev.licenser") version "0.6.1"
    jacoco
    id("io.github.chiragji.jacotura") version "1.1.2"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
}

group = "org.veo"
version = "0.48.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

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
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
    implementation("io.hypersistence:hypersistence-utils-hibernate-62:3.9.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("net.swiftzer.semver:semver:2.0.0")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    val kotestVersion = "5.9.1"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    val testcontainersVersion = "1.20.4"
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
}

tasks.withType<BootRun> {
    project.properties["jvmArgs"]
        ?.let { it as String }
        ?.split(Regex("\\s+"))
        ?.let { jvmArgs = it }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
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
        ktlint("1.5.0") // https://github.com/diffplug/spotless/issues/2349
    }
    kotlinGradle {
        ktlint("1.5.0") // https://github.com/diffplug/spotless/issues/2349
    }
    json {
        target("**/*.json")
        addStep(
            object : FormatterStep {
                override fun getName() = "format json"

                override fun format(
                    rawUnix: String,
                    file: File,
                ): String {
                    val om = ObjectMapper()
                    return om
                        .writer()
                        .with(DefaultPrettyPrinter().apply { indentArraysWith(SYSTEM_LINEFEED_INSTANCE) })
                        .writeValueAsString(om.readValue(rawUnix, Map::class.java))
                }
            },
        )
    }
    yaml {
        target(".gitlab-ci.yml")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

license {
    header.set(resources.text.fromFile("templates/licenseHeader.txt"))
    newLine.set(false)
    skipExistingHeaders.set(true)
    exclude("**/*.properties", "**/*.xml")
    style(
        closureOf<HeaderFormatRegistry> {
            put("kt", "JAVADOC")
        },
    )
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] =
        Git.open(project.rootDir).use {
            it.getRepository().getConfig().getString("user", null, "name") ?: "<name>"
        }
}

// Add no-arg ORM constructors for JPA entities.
noArg {
    annotation("jakarta.persistence.Entity")
}

// Make entity classes open to stop hibernate from complaining
allOpen {
    annotation("jakarta.persistence.Entity")
}

springBoot {
    buildInfo {
        properties {
            if (getRootProject().hasProperty("ciBuildNumer")) {
                additional.set(
                    mapOf(
                        "ci.buildnumber" to rootProject.properties["ciBuildNumer"],
                        "ci.jobname" to rootProject.properties["ciJobName"],
                    ),
                )
            }
        }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(true)
    }
}

jacotura {
    properties {
        property("jacotura.jacoco.path", "$buildDir/reports/jacoco/test/jacocoTestReport.xml")
        property("jacotura.cobertura.path", "$buildDir/reports/cobertura.xml")
    }
}
