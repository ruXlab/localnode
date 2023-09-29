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
            version("web3j", "4.10.3")

            library("dockerjava", "com.github.docker-java:docker-java:3.3.3")

            library("slf4j-api", "org.slf4j:slf4j-api:1.7.32")
            library("logback-classic", "ch.qos.logback:logback-classic:1.2.6")
            bundle("logging", listOf("slf4j-api", "logback-classic"))

            library("web3j", "org.web3j", "core").versionRef("web3j")

        }
        create("testlibs") {
            version("junit", "5.10.0")
            
            library("assertk", "com.willowtreeapps.assertk:assertk:0.27.0")

            library("junit-bom", "org.junit", "junit-bom").versionRef("junit")
            library("junit-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("junit-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit")
        }
    }
}
include("web3j")
include("core")
