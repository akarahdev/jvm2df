plugins {
    id("java")
    id("application")
}

group = "dev.akarah"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.apache.commons:commons-text:1.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.run {
    mainClass = "dev.akarah.jvm2df.generator.Main"
}