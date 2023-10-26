plugins {
    kotlin("jvm") version "1.9.0" apply false
    application
    id("maven-publish")
}

group = "vc.rux.pokefork"
version = "0.0-SNAPSHOT"


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "vc.rux.pokefork"
            artifactId = "pokefork"

            from(components["java"])
        }
    }
}