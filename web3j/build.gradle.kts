import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") 
}

group = "vc.rux.pokefork"
version = "0.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.web3j)
    implementation(libs.bundles.logging)

    testImplementation(libs.web3j.contracts)
    testImplementation(platform(testlibs.junit.bom))
    testImplementation(testlibs.junit.api)
    testImplementation(testlibs.junit.params)
    testRuntimeOnly(testlibs.junit.engine)
    testImplementation(testlibs.assertk)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(
            TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
        )
    }
}