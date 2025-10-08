plugins {
  alias(libs.plugins.spring.boot)
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
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  testImplementation("org.springframework.boot:spring-boot-starter-test")

  implementation(libs.spring.boot.mail)
  implementation(libs.mailgun.java)
  implementation(libs.spring.boot.freemarker)
  implementation(libs.commons.lang3)
}

