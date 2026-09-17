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
  implementation(project(":packages:jpa-data"))

  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation(libs.kotlinx.coroutines.core)

  implementation(libs.spring.boot.web)
  implementation(libs.spring.boot.validation)
  implementation(libs.reactor.core)

  implementation(libs.telegrambots.meta)
  implementation(libs.gson)
  implementation(libs.commons.lang3)
  implementation("org.slf4j:slf4j-api")

  testImplementation(kotlin("test"))
  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.reactor.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
  useJUnitPlatform { excludeTags("unstable", "nlp") }
}
