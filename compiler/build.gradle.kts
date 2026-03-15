plugins {
    id("java")
}

group = "dev.akarah"
version = "unspecified"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.mojang:datafixerupper:9.0.19")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("org.jetbrains:annotations:26.0.2")
}

tasks.test {
    useJUnitPlatform()
}