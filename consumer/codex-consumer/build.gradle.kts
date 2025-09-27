plugins {
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
    testImplementation("io.kotest:kotest-assertions-json:5.7.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")

    // MockK + Spring 어댑터 (추천)
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

val generatedAvroDir = layout.buildDirectory.dir("generated-main-avro-java")

sourceSets {
    named("main") {
        java.srcDir(generatedAvroDir)
    }
}

tasks.named<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    setOutputDir(generatedAvroDir.get().asFile)
    isCreateSetters = false
    fieldVisibility = "PRIVATE"
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    dependsOn("generateAvroJava")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier = ""
    mainClass.set("com.codex.consumer.CodexConsumerApplication")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.withType<Test> {
    useJUnitPlatform()
}
