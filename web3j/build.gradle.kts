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
    implementation(project(mapOf("path" to ":")))
    implementation(libs.web3j)
    implementation(libs.bundles.logging)
    
    testImplementation(platform(testlibs.junit.bom))
    testImplementation(testlibs.junit.api)
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