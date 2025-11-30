plugins {
//  alias(libs.plugins.test.logger)
  alias(libs.plugins.kotlin.jvm)
  kotlin("plugin.serialization") version "1.9.0"
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
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.hibernate.spatial) // For JTS Point type
  testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
  useJUnitPlatform()
//  failOnNoDiscoveredTests = false
}

// Expose test classes
configurations {
  create("testOutput")
}

tasks.register<Jar>("testJar") {
  from(sourceSets.test.get().output)
  archiveClassifier.set("test")
}

artifacts {
  add("testOutput", tasks.named<Jar>("testJar"))
}
