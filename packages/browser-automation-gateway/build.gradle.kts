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
  // the agent protocol is made of generated GraphQL types
  implementation(project(":packages:graphql-api"))

  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.reactor)

  implementation(libs.reactor.core)
  implementation(libs.gson)
  implementation("io.micrometer:micrometer-core")
  implementation("jakarta.annotation:jakarta.annotation-api")
  implementation("org.slf4j:slf4j-api")

  testImplementation(kotlin("test"))
  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.spring.boot.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
