plugins {
//  alias(libs.plugins.test.logger)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kapt)
  `java-test-fixtures`
}

repositories {
  mavenCentral()
//  gradlePluginPortal()
}

kotlin { jvmToolchain(21) }

dependencies {
  implementation(platform(libs.spring.boot.bom))

  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation(project(":packages:domain"))

  implementation(libs.kotlinx.coroutines.core)
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

  testFixturesImplementation(platform(libs.spring.boot.bom))
  testFixturesApi("org.junit.jupiter:junit-jupiter-api")
  testFixturesImplementation(libs.testcontainers.core)
  testFixturesImplementation(libs.testcontainers.postgresql)
  testFixturesImplementation(libs.testcontainers.junit)
  testFixturesImplementation("org.slf4j:slf4j-api")

  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.testcontainers.core)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.junit)
  testImplementation(libs.flyway.core)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.test {
  useJUnitPlatform()
//  failOnNoDiscoveredTests = false
}
