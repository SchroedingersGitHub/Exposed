import org.jetbrains.exposed.gradle.Versions
plugins {
    kotlin("jvm") apply true
}

group = "org.jetbrains.exposed"
version = "0.39.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":exposed-core"))
    implementation(project(":exposed-dao"))
    implementation(project(":exposed-jdbc"))

    implementation("org.slf4j", "slf4j-api", Versions.slf4j)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", Versions.log4j2)
    implementation("org.apache.logging.log4j", "log4j-api", Versions.log4j2)
    implementation("org.apache.logging.log4j", "log4j-core", Versions.log4j2)

    implementation("com.h2database", "h2", Versions.h2)
    implementation("mysql:mysql-connector-java:8.0.30")
    implementation("com.zaxxer:HikariCP:3.4.2")
}


