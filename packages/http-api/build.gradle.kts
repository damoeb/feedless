plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.openapi.generator)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":packages:domain"))
  implementation(libs.gson)
  implementation(libs.spring.boot.web)
  implementation(libs.spring.boot.validation)
  implementation(libs.spring.boot.security)
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.reactor)
  compileOnly("io.swagger.core.v3:swagger-annotations:2.2.30")
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

val openApiPackage = "org.migor.feedless.http.api"
val generatedDir = layout.buildDirectory.dir("generated/sources/openapi")

openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set("$projectDir/src/main/resources/openapi/openapi.yaml")
  outputDir.set(generatedDir.get().asFile.absolutePath)
  apiPackage.set(openApiPackage)
  modelPackage.set("$openApiPackage.model")
  packageName.set(openApiPackage)
  configOptions.set(
    mapOf(
      "interfaceOnly" to "true",
      "useSpringBoot3" to "true",
      "useTags" to "true",
      "documentationProvider" to "none",
      "annotationLibrary" to "none",
      "enumPropertyNaming" to "original",
      "serviceInterface" to "false",
      "skipDefaultInterface" to "true",
      "reactive" to "true",
      "useCoroutines" to "true",
      "gradleBuildFile" to "false",
    )
  )
}

kotlin.sourceSets["main"].kotlin.srcDir(generatedDir.map { it.dir("src/main/kotlin") })

tasks.named("compileKotlin") {
  dependsOn(tasks.named("openApiGenerate"))
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

tasks.test {
  useJUnitPlatform()
}
