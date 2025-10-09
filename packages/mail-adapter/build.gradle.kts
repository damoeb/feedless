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
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation(libs.spring.boot.mail)
  implementation(libs.mailgun.java)
  implementation(libs.spring.boot.freemarker)
  implementation(libs.spring.boot.web)
  testImplementation(libs.spring.boot.test)
  implementation(libs.commons.lang3)
}

sourceSets {
  main {
    java.srcDirs("src/main/kotlin")
  }
  test {
    java.srcDirs("src/test/kotlin")
  }
}

// ✅ Add this to enable JUnit 5
tasks.test {
  useJUnitPlatform()
}
