import com.diffplug.spotless.FormatterStep
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.TextReportRenderer
import com.github.jk1.license.task.ReportTask
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.eclipse.jgit.api.Git
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Calendar

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"

    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.0"

    id("com.diffplug.spotless") version "6.20.0"
    id("org.cadixdev.licenser") version "0.6.1"
    jacoco
    id("io.github.chiragji.jacotura") version "1.1.2"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("com.github.jk1.dependency-license-report") version "2.5"
}

group = "org.veo"
version = "0.30.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation("org.springframework.security:spring-security-test")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("io.mockk:mockk:1.13.7")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("net.swiftzer.semver:semver:1.3.0")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    val kotestVersion = "5.6.2"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    val testcontainersVersion = "1.18.3"
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
}

val licenseFile3rdParty = "LICENSE-3RD-PARTY.txt"
licenseReport {
    renderers = arrayOf(
        TextReportRenderer(licenseFile3rdParty),
    )
    projects = arrayOf(project)
    filters = arrayOf(
        LicenseBundleNormalizer(),
    )
}

val reportTask = tasks.getByName("generateLicenseReport") as ReportTask
// work around for license report not being updated when the project's version number changes
// https://github.com/jk1/Gradle-License-Report/issues/223
reportTask.outputs.apply {
    upToDateWhen { false }
    cacheIf { false }
}
task("copy3rdPartyLicenseFile") {
    reportTask.finalizedBy(this)
    doLast {
        file(licenseFile3rdParty).writeText(file("${reportTask.config.outputDir}/$licenseFile3rdParty").readText())
    }
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
    exclude("**/*.properties")
    style(
        closureOf<HeaderFormatRegistry> {
            put("kt", "JAVADOC")
        },
    )
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] = Git.open(project.rootDir).use {
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
