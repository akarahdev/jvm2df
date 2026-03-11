plugins {
    id("java")
}

group = "dev.akarah"
version = "unspecified"

repositories {
    mavenCentral()
}

sourceSets {
    // `patched` source-set is so we can overwrite java standard libraries when compiling
    // to DiamondFire
    val patched by creating {
        java.srcDir("src/patched/java")
    }
    main {
        java.srcDir("src/main/java")
        compileClasspath += patched.output
        runtimeClasspath += patched.output
    }
}

tasks.named<JavaCompile>("compilePatchedJava") {
    options.compilerArgs.addAll(listOf("--patch-module", "java.base=${file("src/patched/java").absolutePath}"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn("compilePatchedJava")
    options.compilerArgs.addAll(listOf("--patch-module", "java.base=${sourceSets["patched"].output.asPath}"))
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    from(sourceSets["patched"].output)
}