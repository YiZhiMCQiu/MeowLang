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