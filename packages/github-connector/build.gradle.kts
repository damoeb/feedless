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
  implementation(libs.spring.boot.web)
  testImplementation(libs.spring.boot.test)
  implementation(libs.commons.lang3)
  // https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
  implementation("org.eclipse.jgit:org.eclipse.jgit:7.4.0.202509020913-r")
}

tasks.test {
  useJUnitPlatform()
//  failOnNoDiscoveredTests = false
}
