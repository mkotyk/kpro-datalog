import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `application`
    kotlin("jvm") version "1.4.32"
}

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(project(":library"))
    implementation(files("libs/javax-usb3-1.4.0.jar"))
    implementation(files("libs/usb3-ftdi-1.3.0.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta1")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit:junit:4.13.1")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}