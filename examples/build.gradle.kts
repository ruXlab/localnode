plugins {
    kotlin("jvm")
}

group = "vc.rux.pokefork.examples"
version = "0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") // optional for snapshots
}

dependencies {
    implementation("vc.rux.pokefork:web3j:0.1.0")
    implementation("org.web3j:core:4.10.3")
}


kotlin {
    
}



