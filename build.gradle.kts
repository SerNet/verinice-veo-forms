import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "4.0.2"

    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.3.0"

    id("com.diffplug.spotless") version "8.1.0"
    jacoco
    id("io.github.chiragji.jacotura") version "1.1.2"
    id("com.gorylenko.gradle-git-properties") version "2.5.4"
}

group = "org.veo"
version = "0.66.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
    implementation("io.hypersistence:hypersistence-utils-hibernate-71:3.14.1")
    // required by hypersistence-utils-hibernate-71
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("net.swiftzer.semver:semver:2.1.0")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    val kotestVersion = "6.1.1"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("io.mockk:mockk:1.14.7")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")

    testRuntimeOnly("org.springframework.boot:spring-boot-starter-security-test")
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

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict ", "-Xannotation-default-target=param-property")
    }
}

spotless {
    format("misc") {
        target("**/*.md", "**/*.gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin {
        target("buildSrc/**/*.kt", "src/**/*.kt")
        addStep(
            org.veo.forms.LicenseHeaderStep
                .create(project.rootDir),
        )
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
    json {
        target("**/*.json")
        gson().indentWithSpaces(2)
        endWithNewline()
    }
    yaml {
        target(".gitlab-ci.yml")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
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
            if (getRootProject().hasProperty("ciBuildNumber")) {
                additional.set(
                    mapOf(
                        "ci.buildnumber" to rootProject.properties["ciBuildNumber"] as String,
                        "ci.jobname" to rootProject.properties["ciJobName"] as String,
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
        property("jacotura.jacoco.path", "${layout.buildDirectory}/reports/jacoco/test/jacocoTestReport.xml")
        property("jacotura.cobertura.path", "${layout.buildDirectory}/reports/cobertura.xml")
    }
}
