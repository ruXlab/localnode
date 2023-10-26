import java.net.URI

plugins {
    id("java")
}

group = "vc.rux.pokefork"
version = "0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    implementation("com.github.ruXlab:web3j:v0.0.1-test")
    
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}