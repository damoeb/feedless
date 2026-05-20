plugins {
  alias(libs.plugins.openapi.generator)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
}

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))

dependencies {
  implementation(libs.spring.boot.web)
  implementation(libs.spring.boot.validation)
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
  implementation("jakarta.validation:jakarta.validation-api:3.1.0")
  implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set(layout.projectDirectory.file("src/main/resources/openapi/openapi.yaml").asFile.path)
  outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
  apiPackage.set("org.migor.feedless.http.api")
  modelPackage.set("org.migor.feedless.http.model")
  invokerPackage.set("org.migor.feedless.http")
  configOptions.set(
    mapOf(
      "interfaceOnly" to "true",
      "useSpringBoot3" to "true",
      "useTags" to "true",
      "gradleBuildFile" to "false",
      "documentationProvider" to "none",
      "enumPropertyNaming" to "UPPERCASE",
      "serializationLibrary" to "jackson",
      "useBeanValidation" to "true",
    )
  )
}

tasks.named("compileKotlin") {
  dependsOn(tasks.openApiGenerate)
}
