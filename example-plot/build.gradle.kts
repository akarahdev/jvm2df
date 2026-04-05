plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.2"
    id("io.github.sgtsilvio.gradle.proguard") version "0.9.0"
}

group = "dev.akarah"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stdlib"))
}

tasks.jar {
    from(
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    )
}


val proguardJar by tasks.registering(proguard.taskClass) {
    val usageReport = layout.buildDirectory.file("libs/${project.name}-${project.version}-usage.txt")

    addInput {
        classpath.from(tasks.shadowJar)
    }
    addOutput {
        archiveFile.set(base.libsDirectory.file("${project.name}-${project.version}-proguarded.jar"))
    }
    jdkModules.add("java.base")

    rules.addAll(
        "-dontoptimize",
        "-dontobfuscate",
        "-dontwarn",
        "-ignorewarnings",
        "-printusage ${usageReport.get().asFile.absolutePath}",
        "-keep class * extends df.event.PlayerEventHandler { *; }",
        "-keepattributes Signature,InnerClasses,*Annotation*"
    )
}

tasks.assemble {
    dependsOn(proguardJar)
}

