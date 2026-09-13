plugins {
  // https://github.com/Netflix/dgs-framework/blob/v8.7.1/graphql-dgs-client/dependencies.lock
  alias(libs.plugins.dgs.codegen)

  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kapt)
}

repositories {
  mavenCentral()
}

kotlin { jvmToolchain(21) }

//sourceSets.getByName("main") {
////  java.srcDir("src/main/java")
////  java.srcDir("src/generated/java")
//  kotlin.srcDir(layout.buildDirectory.dir("generated/sources/dgs-codegen"))
////  java.srcDir("src/main/kotlin")
//  resources.srcDir("src/main/resources")
//}
kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/sources/dgs-codegen"))


dependencies {
  implementation(platform(libs.spring.boot.bom))
  implementation(platform(libs.dgs.platform))
  implementation(project(":packages:domain"))
  implementation(libs.spring.boot.web)
  implementation(libs.spring.boot.security)
  implementation(libs.dgs.starter)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.reactor)
  implementation(libs.commons.lang3)
  implementation(libs.xsoup)
  // EmailValidatorDirective; excludes match languagetool's, so no vulnerable commons-beanutils ships
  implementation("commons-validator:commons-validator:1.9.0") {
    exclude(group = "commons-beanutils")
    exclude(group = "commons-collections")
  }
  implementation("org.mapstruct:mapstruct:1.6.3")
  kapt("org.mapstruct:mapstruct-processor:1.6.3")

  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation(libs.dgs.codegen.test)
}

// kapt stubs must see the DGS-generated types
tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask> { dependsOn("generateJava") }

tasks.test {
  useJUnitPlatform()
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

// https://netflix.github.io/dgs/generating-code-from-schema/
val generateKotlin = tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
  schemaPaths = mutableListOf(
    "$projectDir/src/main/resources/schema/schema.graphqls"
  )
  typeMapping = mutableMapOf("Upload" to "org.springframework.web.multipart.MultipartFile")
  packageName = "org.migor.feedless.generated"
  generateInterfaces = false
  generateClient = true
  generateDataTypes = true
  language = "kotlin"
//  generateKotlinNullableClasses = true
  generateKotlinClosureProjections = true
}

//val restCodegen = tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
//  generatorName.set("spring")
//  inputSpec.set(project.file("src/main/resources/schema/openapi.yaml").path)
//  outputDir.set("$buildDir/generated")
//  apiPackage.set("org.migor.feedless.api")
////  modelPackage.set("com.example.model")
////  invokerPackage.set("com.example.invoker")
//
//  configOptions.putAll(
//    mapOf(
//      "dateLibrary" to "java8",
////      "library" to "spring-boot",
//      "enumPropertyNaming" to "camelCase",
//      "useTags" to "true"
//    )
//  )
//}

tasks.named("compileKotlin") {
  dependsOn(generateKotlin)
}

