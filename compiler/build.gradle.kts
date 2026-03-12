plugins {
    id("java")
    id("application")
}

group = "dev.akarah"
version = "unspecified"



tasks.run {
    dependsOn(":example-plot:shadowJar")
}

application {
    mainClass = "dev.akarah.jvm2df.Main"
}

tasks.run {
    args(project.rootDir.toString() + "/example-plot/build/libs/example-plot-1.0-SNAPSHOT-all.jar")
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.mojang:datafixerupper:9.0.19")
}

tasks.test {
    useJUnitPlatform()
}