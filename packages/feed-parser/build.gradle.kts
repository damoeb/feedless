plugins {
  alias(libs.plugins.test.logger)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

dependencies {
  implementation(platform(libs.spring.boot.bom))
  implementation(project(":packages:domain"))

  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.kotlinx.coroutines.test)

  // Spring
  implementation(libs.spring.boot.freemarker)
  implementation(libs.spring.boot.web)
  implementation(libs.spring.boot.jpa)
  testImplementation(libs.spring.boot.test)

  // Feed parsing and generation
  implementation(libs.rome)
  implementation(libs.rome.modules)
  implementation(libs.gson)
  implementation(libs.ical4j)
  implementation(libs.broken.xml)

  // HTML/XML parsing
  implementation(libs.jsoup)

  // Utilities
  implementation(libs.commons.lang3)
  implementation(libs.commons.io)

  // Logging
  implementation("org.slf4j:slf4j-api")

  // JTS for geospatial (for Point classes)
  implementation("org.locationtech.jts:jts-core:1.19.0")

  // Testing
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
