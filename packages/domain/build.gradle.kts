plugins {
  alias(libs.plugins.test.logger)
  alias(libs.plugins.kotlin.jvm)
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
}

