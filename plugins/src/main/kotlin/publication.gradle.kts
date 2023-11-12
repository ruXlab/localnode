import java.util.*

plugins {
    `maven-publish`
    signing
}


ext["signing.key"] = null
ext["signing.password"] = null
ext["sonatype.username"] = null
ext["sonatype.password"] = null

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.key"] = System.getenv("SIGNING_KEY")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["sonatype.username"] = System.getenv("SONATYPE_USERNAME")
    ext["sonatype.password"] = System.getenv("SONATYPE_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")


publishing {
    repositories {
        maven {
            name = "sonatype"
            url = uri(
                if (isReleaseBuild) {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                } else {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                }
            )

            credentials {
                username = getExtraString("sonatype.username")
                password = getExtraString("sonatype.password")
            }
        }
    }

    // Creating maven artifacts for jvm
    publications {
        if (project.plugins.hasPlugin("java") && !project.plugins.hasPlugin("application")) { //  && project.name == "web3j"
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        val artifactName: String by project
        val artifactDesc: String by project
        val artifactUrl: String by project
        val artifactScm: String by project
        val artifactLicenseName: String by project
        val artifactLicenseUrl: String by project
        val artifactPublishVersion: String by project

        artifactId = project.name

        // was there
//        artifact(javadocJar)

        pom {
            name.set(artifactName)
            description.set(artifactDesc)
            url.set(artifactUrl)
            version = artifactPublishVersion
            licenses {
                license {
                    name.set(artifactLicenseName)
                    url.set(artifactLicenseUrl)
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("ruX")
                    url.set("https://rux.vc")
                }
            }
            contributors {
            }
            scm {
                connection.set(artifactScm)
                developerConnection.set(artifactScm)
                url.set(artifactUrl)
            }
        }
    }
}

signing {
    val signingKey = project.ext["signing.key"] as? String
    val signingPassword = project.ext["signing.password"] as? String
    val keyFilePath = project.ext["signing.secretKeyRingFile"] as? String

    if (signingKey != null && signingPassword != null && keyFilePath != null) {
        val keyContent = File(keyFilePath).readText()

        useInMemoryPgpKeys(signingKey, keyContent, signingPassword)
        sign(publishing.publications)
    }
}

// TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
}