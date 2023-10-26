plugins {
    kotlin("jvm") version "1.9.0" apply false
    application
    id("maven-publish")
}

group = "vc.rux.pokefork"
version = "0.0-SNAPSHOT"

allprojects {
    group = "vc.rux.pokefork"
}

subprojects {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name

                from(components["java"])
            }
        }
    }
}
