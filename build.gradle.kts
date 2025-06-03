plugins {
    id("java")
    id("application")
    alias(libs.plugins.shadow)
}

group = "org.glavo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kala.ansi)
    implementation(libs.kala.template)
    implementation(libs.poi.ooxml)
    implementation(libs.jline)
    implementation(libs.log4j.core)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("org.glavo.meow.Main")
}

tasks.compileJava {
    options.release.set(21)
}

tasks.jar {
    manifest.attributes(
        "Main-Class" to "org.glavo.meow.Main",
    )
}

tasks.test {
    useJUnitPlatform()
}

val meowSh by tasks.registering {
    val headerFile = file("src/main/header/header.sh")
    val outputFile = layout.buildDirectory.dir("libs").map { it.file("meow-$version.sh") }

    dependsOn(tasks.shadowJar)
    inputs.file(headerFile)
    inputs.file(tasks.shadowJar.map { it.archiveFile })
    outputs.file(outputFile)

    doLast {
        outputFile.get().asFile.outputStream().use {
            it.write(headerFile.readBytes())
            it.write(tasks.shadowJar.get().archiveFile.get().asFile.readBytes())
        }
        outputFile.get().asFile.setExecutable(true)
    }
}