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
  implementation(project(":packages:freemarker-templates"))

  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  testImplementation(project(":packages:domain", "testOutput"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.kotlinx.coroutines.test)
  implementation(libs.spring.boot.mail)
  implementation(libs.mailgun.java)
  implementation(libs.spring.boot.web)
  testImplementation(libs.spring.boot.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  implementation(libs.commons.lang3)
}

tasks.test {
  useJUnitPlatform()
}
