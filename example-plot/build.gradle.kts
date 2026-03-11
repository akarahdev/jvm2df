plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.2"
}

group = "dev.akarah"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stdlib"))
}