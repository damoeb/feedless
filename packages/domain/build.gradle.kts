plugins {
//  alias(libs.plugins.test.logger)
  alias(libs.plugins.kotlin.jvm)
//  alias(libs.plugins.kotlin.spring)
}

repositories {
  mavenCentral()
//  gradlePluginPortal()
}

dependencies {
//  implementation(platform(libs.spring.boot.bom))

  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.gson)
  testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
  useJUnitPlatform()
//  failOnNoDiscoveredTests = false
}
