plugins {
    id("java")
    id("application")
}

group = "dev.akarah"
version = "unspecified"



application {
    mainClass = "dev.akarah.jvm2df.frontend.Main"
}

tasks.run {
    dependsOn(":example-plot:proguardJar")
}

tasks.run {
    args(project.rootDir.toString() + "/example-plot/build/libs/example-plot-1.0-SNAPSHOT-proguarded.jar")
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation(project(":compiler"))
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
}