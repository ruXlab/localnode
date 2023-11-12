import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    id("publication")
    `maven-publish`
    signing
}

group = "vc.rux.pokefork"
version = "0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.dockerjava)
    implementation(libs.bundles.logging)

    implementation(libs.web3j)

    testImplementation(kotlin("test"))
    testImplementation(platform(testlibs.junit.bom))
    testImplementation(testlibs.junit.api)
    testImplementation(testlibs.junit.params)
    testRuntimeOnly(testlibs.junit.engine)
    testImplementation(testlibs.assertk)
    testImplementation(libs.web3j.contracts)
}


java {
    withJavadocJar()
    withSourcesJar()
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(
            TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
        )
    }
}