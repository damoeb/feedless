import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
//  id("org.openapi.generator") version "7.9.0" // rest
//  id("com.google.protobuf") version "0.9.2"

  alias(libs.plugins.spring.boot)
  // https://github.com/Netflix/dgs-framework/blob/v8.7.1/graphql-dgs-client/dependencies.lock
  alias(libs.plugins.dgs.codegen)
  alias(libs.plugins.test.logger)
  alias(libs.plugins.grgit)
  alias(libs.plugins.jacoco)
  alias(libs.plugins.javacc)

  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
}

apply(plugin = "io.spring.dependency-management")

group = "org.migor.feedless"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

sourceSets.getByName("main") {
  java.srcDir("src/main/java")
  java.srcDir("src/generated/java")
//  java.srcDir("$buildDir/generated/src/main/java")
//  java.srcDir("src/main/kotlin")
  resources.srcDir("src/main/resources")
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
//    xml.required.set(true)
//    csv.required.set(false)
    html.required.set(true)
  }
}

//tasks.jacocoTestCoverageVerification {
//  violationRules {
//    rule {
//      limit {
//        minimum = "0.80".toBigDecimal() // Minimum 80% coverage required
//      }
//    }
//  }
//}

tasks.test {
  val osName = System.getProperty("os.name").lowercase()
  if (osName.contains("linux")) {
//    val process = ProcessBuilder("id", "-u").start()
//    val uid = process.inputStream.bufferedReader().readText().trim()
//    environment("DOCKER_HOST", "unix:///run/user/$uid/podman/podman.sock")
    environment("DOCKER_HOST", "unix:///var/run/docker.sock")
  } else {
    throw IllegalArgumentException("test currently only run on linux")
  }
//  } else if (os.isMacOsX) {
//    environment("DOCKER_HOST", "unix:///tmp/podman.sock")
//  }
//  environment("TESTCONTAINERS_RYUK_DISABLED", "true")
}

tasks.check {
  dependsOn(tasks.jacocoTestCoverageVerification)
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

dependencies {
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation(libs.kotlin.reflect)
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.kotlinx.coroutines.test)
  implementation(libs.kotlinx.coroutines.reactor)
//  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${versions["kotlinxCoroutines"]}")
  implementation(libs.spring.boot.jpa)
//  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation(libs.spring.boot.web)
//  implementation("org.springframework:spring-aspects")
  implementation(libs.spring.boot.validation)
  implementation(libs.spring.boot.webflux)
  implementation(libs.spring.boot.freemarker)
//  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation(libs.tika.core)
  implementation(libs.pdfbox.tools)
  implementation(libs.pdf2dom)
  implementation(libs.bucket4j.core)
//  implementation("org.redundent:kotlin-xml-builder:1.7.4")
  // https://mvnrepository.com/artifact/org.apache.commons/commons-text
  implementation(libs.commons.text)
  implementation(libs.webp.imageio.sejda)


  // graphql
//  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation(libs.spring.boot.websocket)
  implementation("org.springframework.security:spring-security-messaging")
  implementation(platform(libs.dgs.platform))
  implementation(libs.dgs.starter)
  implementation(libs.dgs.scalars)
  implementation(libs.dgs.subscriptions)
  implementation(libs.dgs.subscriptions.autoconfigure)
  testImplementation(libs.spring.graphql.test)
//  implementation("org.mapstruct:mapstruct:1.5.5.Final")
//  annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

  // cache
  implementation(libs.spring.boot.cache)
  implementation("org.ehcache:ehcache:3.10.8")

  // monitoring
  implementation(libs.spring.boot.actuator)

  // mail
  implementation(libs.spring.boot.mail)
  implementation(libs.mailgun.java)
  // https://github.com/micrometer-metrics/micrometer
  implementation("io.micrometer:micrometer-registry-prometheus")
//  implementation("com.github.loki4j:loki-logback-appender:1.3.2")
  implementation(libs.logstash.logback.encoder)

  // security
  implementation(libs.spring.boot.security)
  implementation(libs.spring.boot.oauth)
  // https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
  implementation(libs.nimbus.jose.jwt)

  // json feed
  implementation(libs.json)
  implementation(libs.guava)
  implementation(libs.ical4j)


  implementation(libs.commons.lang3)
  implementation(libs.commons.io)

  // readability
  implementation(libs.readability4j)

  // reactor
  // https://mvnrepository.com/artifact/io.projectreactor/reactor-core
  implementation(libs.reactor.core)
  implementation(libs.reactor.test)

  // database
  implementation(libs.postgresql)
  implementation(libs.hibernate.spatial)
  implementation(libs.kotlin.jdsl.jpql.dsl)
  implementation(libs.kotlin.jdsl.jpql.render)
  implementation(libs.kotlin.jdsl.spring.support)


  // text
  // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analysis-common
  implementation(libs.lucene.analysis.common)
  implementation(libs.opennlp.tools)
  implementation(libs.commons.math3)
  testImplementation(libs.jenetics)
  implementation(libs.language.en)
  implementation(libs.language.de)

//  https://dzone.com/articles/build-a-spring-boot-app-with-flyway-and-postgres
  implementation(libs.flyway.core)

  implementation(libs.async.http.client)
  implementation(libs.broken.xml)
  implementation(libs.rome)
  implementation(libs.rome.modules)
  implementation(libs.jsoup)
  implementation(libs.xsoup)
  implementation(libs.gson)

  // https://github.com/shyiko/skedule
//  implementation("com.github.shyiko.skedule:skedule:0.4.0")

  testImplementation(libs.spring.boot.test)
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testCompileOnly("org.junit.jupiter:junit-jupiter-params")
  implementation("org.junit.jupiter:junit-jupiter")
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.core)
  testImplementation(libs.testcontainers.junit)

  // Property-Based-Testing https://mvnrepository.com/artifact/net.jqwik/jqwik
  testImplementation(libs.jqwik)
  implementation(libs.telegrambots.spring.boot.starter)

  // payments
  implementation("com.stripe:stripe-java:29.1.0")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
  this.archiveFileName.set("app.${archiveExtension.get()}")
}

// https://netflix.github.io/dgs/generating-code-from-schema/
val graphqlCodegen = tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
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

val codegen = tasks.register("codegen") {
  dependsOn(graphqlCodegen)
}

tasks.named<JavaCompile>("compileJava") {
  dependsOn(graphqlCodegen)
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
//    freeCompilerArgs = listOf("-Xjsr305=strict")
//    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform {
    val tags = arrayOf("unstable", "nlp")
    println("Excluding tests with tags [${tags.joinToString(", ")}]")
    excludeTags(*tags)
  }
  finalizedBy(tasks.getByName("jacocoTestReport"))
}

//val fetchGithubJars = tasks.register("fetchGithubJars", Exec::class) {
//  commandLine("sh", "./fetchGithubJars.sh")
//}
val compilejj = tasks.getByName<org.javacc.plugin.gradle.javacc.CompileJavaccTask>("compileJavacc") {
  inputDirectory = file("src/main/kotlin/org/migor/feedless/document/filter")
  outputDirectory = file("src/generated/java/org/migor/feedless/document/filter/generated")
}

tasks.getByName("compileKotlin").dependsOn(compilejj, codegen)
tasks.getByName("compileTestKotlin").dependsOn(compilejj, codegen)

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
  systemProperty("APP_BUILD_TIMESTAMP", Date().time)
  args("--spring.profiles.active=dev ")
}

val lintTask = tasks.register("lint") {
//  todo mag
//  dependsOn("lintDockerImage")
}
tasks.register("start") {
  dependsOn("codegen", "bootRun")
}

val buildTask = tasks.findByPath("build")!!.dependsOn(lintTask, "test", "bootJar")

val dockerAmdBuild = tasks.register("buildAmdDockerImage", Exec::class) {
  dependsOn(buildTask)
  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")
  val gitHash = grgit.head().id.take(7)

  environment("DOCKER_CLI_EXPERIMENTAL", "enabled")

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)
  inputs.property("semver", semver)

  commandLine(
    podmanOrDocker(), "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "--build-arg", "APP_BUILD_TIMESTAMP=${Date().time}",
    "--platform=linux/amd64",
    "-t", "$baseTag:core-latest",
    "-t", "$baseTag:core-$gitHash",
    "."
  )
}

tasks.register("bundle") {
  dependsOn(dockerAmdBuild)
}

fun podmanOrDocker(): String {
  val env = "DOCKER_BIN"
  val podmanOrDocker = System.getenv(env) ?: "docker"

  println("Using DOCKER_BIN $podmanOrDocker")
  return podmanOrDocker
}
