pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "pokefork"

// create library versions and bundles
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {

            library("dockerjava", "com.github.docker-java:docker-java:3.3.3")

            library("slf4j-api", "org.slf4j:slf4j-api:1.7.32")
            library("logback-classic", "ch.qos.logback:logback-classic:1.2.6")
            bundle("logging", listOf("slf4j-api", "logback-classic"))

        }
        create("testlibs") {
        }
    }
}
