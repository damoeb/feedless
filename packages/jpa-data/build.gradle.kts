plugins {
//  alias(libs.plugins.test.logger)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kapt)
}

repositories {
  mavenCentral()
//  gradlePluginPortal()
}

dependencies {
  implementation(platform(libs.spring.boot.bom))

  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation(project(":packages:domain"))

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.gson)
  testImplementation(libs.kotlinx.coroutines.test)
  implementation(libs.spring.boot.validation)
  implementation(libs.commons.text)
  implementation(libs.postgresql)
  implementation(libs.hibernate.spatial)
  implementation(libs.kotlin.jdsl.jpql.dsl)
  implementation(libs.kotlin.jdsl.jpql.render)
  implementation(libs.kotlin.jdsl.spring.support)
  implementation(libs.spring.boot.jpa)

  implementation("org.mapstruct:mapstruct:1.6.3")
  kapt("org.mapstruct:mapstruct-processor:1.6.3")

  implementation(libs.jsoup)
  implementation(libs.xsoup)
}

tasks.test {
  useJUnitPlatform()
//  failOnNoDiscoveredTests = false
}
