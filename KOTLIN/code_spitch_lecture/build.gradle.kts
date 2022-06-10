import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    application
}

group = "me.bongholee"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    testImplementation ("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// 새로나온 gradle의 플러그인 -> gradle 명령어 중 application task가 생긴다.
application {
    mainClass.set("MainKt")
}