import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.4.32"
}
repositories {
    mavenCentral()
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.32")
    testImplementation("junit:junit:4.13")
    implementation(kotlin("stdlib-jdk8"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}