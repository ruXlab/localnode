plugins {
    kotlin("jvm")
}

group = "vc.rux.localnode.examples"
version = "0.0-SNAPSHOT"

repositories {
    mavenCentral()
//    maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/") // optional for releases
//    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") // optional for snapshots
//    maven(url = "https://s01.oss.sonatype.org/content/groups/staging/") // optional for staging
}

dependencies {
    implementation("vc.rux.localnode:localnode:0.1.0")
    implementation("org.web3j:core:4.10.3")
}


kotlin {
    
}



