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

  implementation(libs.spring.boot.jpa)

  // scoring: tokenizing, stemming, part-of-speech, language detection, spelling
  implementation(libs.lucene.analysis.common)
  implementation(libs.opennlp.tools)
  implementation(libs.commons.math3)
  implementation(libs.language.en)
  implementation(libs.language.de)

  implementation("org.slf4j:slf4j-api")

  testImplementation(kotlin("test"))
  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.spring.boot.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
  // same exclusions as server-core, where this code came from: the nlp tests need
  // OpenNLP model files that are not in the repo
  useJUnitPlatform { excludeTags("unstable", "nlp") }
}
