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

    testImplementation ("io.kotest:kotest-runner-junit5:5.1.0")
    testImplementation ("io.kotest:kotest-assertions-core:5.1.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

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