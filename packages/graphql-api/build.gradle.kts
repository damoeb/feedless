plugins {
  alias(libs.plugins.spring.boot)
  // https://github.com/Netflix/dgs-framework/blob/v8.7.1/graphql-dgs-client/dependencies.lock
  alias(libs.plugins.dgs.codegen)

  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
}

repositories {
  mavenCentral()
}

//sourceSets.getByName("main") {
////  java.srcDir("src/main/java")
////  java.srcDir("src/generated/java")
//  kotlin.srcDir(layout.buildDirectory.dir("generated/sources/dgs-codegen"))
////  java.srcDir("src/main/kotlin")
//  resources.srcDir("src/main/resources")
//}
kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/sources/dgs-codegen"))


dependencies {
  implementation(libs.spring.boot.web)
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

