import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "vc.rux.pokefork"
version = "0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.dockerjava)
    implementation(libs.bundles.logging)

    testImplementation(kotlin("test"))
    testImplementation(platform(testlibs.junit.bom))
    testImplementation(testlibs.junit.api)
    testImplementation(testlibs.junit.params)
    testRuntimeOnly(testlibs.junit.engine)
    testImplementation(testlibs.assertk)

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}


