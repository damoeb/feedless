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

kotlin { jvmToolchain(21) }

dependencies {
  implementation(platform(libs.spring.boot.bom))
  api(project(":packages:domain"))
  implementation(project(":packages:feed-parser"))
  // the feed engine works on the generated GraphQL types; see docs/plans/2026-09-17-feed-module.md
  implementation(project(":packages:graphql-api"))

  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation(libs.kotlinx.coroutines.core)

  implementation(libs.spring.boot.web)

  implementation(libs.gson)
  implementation(libs.jsoup)
  implementation(libs.xsoup)
  implementation(libs.commons.lang3)
  implementation(libs.commons.text)
  implementation("org.slf4j:slf4j-api")

  testImplementation(kotlin("test"))
  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.kotlinx.coroutines.reactor)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
