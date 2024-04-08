import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
  id("org.springframework.boot") version "3.0.13"
  id("com.adarshr.test-logger") version "3.2.0"
  id("com.netflix.dgs.codegen") version "6.1.5"
  id("org.ajoberstar.grgit")
//  id("com.google.protobuf") version "0.9.2"
  kotlin("jvm") version "1.9.20"
  kotlin("plugin.spring") version "1.9.20"
}

apply(plugin = "io.spring.dependency-management")

group = "org.migor.feedless"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

sourceSets.getByName("main") {
  java.srcDir("src/main/java")
  java.srcDir("src/main/kotlin")
  resources.srcDir("src/main/resources")
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

val versions = mapOf(
  "kotlinxCoroutines" to "1.6.0",
//  "grpc" to "1.53.0",
  "dgs" to "6.0.5"
)

dependencies {
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["kotlinxCoroutines"]}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${versions["kotlinxCoroutines"]}")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework:spring-aspects")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-freemarker")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation("org.apache.tika:tika-core:2.9.0")
  implementation("org.apache.pdfbox:pdfbox-tools:2.0.29")
  implementation("net.sf.cssbox:pdf2dom:2.0.3")
  implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.5.0")
  implementation("org.redundent:kotlin-xml-builder:1.7.4")
  // https://mvnrepository.com/artifact/org.apache.commons/commons-text
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("org.sejda.webp-imageio:webp-imageio-sejda:0.1.0")


  // graphql
//  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation("org.springframework.boot:spring-boot-starter-websocket")
  implementation("org.springframework.security:spring-security-messaging")
  implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${versions["dgs"]}"))
  implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:${versions["dgs"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars:${versions["dgs"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets:${versions["dgs"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets-autoconfigure:${versions["dgs"]}")
  testImplementation("org.springframework.graphql:spring-graphql-test:1.2.3")
//  implementation("org.mapstruct:mapstruct:1.5.5.Final")
//  annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

  // cache
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.ehcache:ehcache:3.10.8")

  // monitoring
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-mail")
  // https://github.com/micrometer-metrics/micrometer
  implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
//  implementation("com.github.loki4j:loki-logback-appender:1.3.2")

  // security
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  // json feed
  implementation("org.json:json:20240303")
  implementation("com.google.guava:guava:33.1.0-jre")

  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("commons-io:commons-io:2.11.0")

  // readability
  implementation("net.dankito.readability4j:readability4j:1.0.8")

  // reactor
  // https://mvnrepository.com/artifact/io.projectreactor/reactor-core
  implementation("io.projectreactor:reactor-core:3.5.0")

  // database
  implementation("org.postgresql:postgresql:42.7.3")
  implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.7.3")


//  https://dzone.com/articles/build-a-spring-boot-app-with-flyway-and-postgres
  implementation("org.flywaydb:flyway-core:9.16.1")

  implementation("org.asynchttpclient:async-http-client:2.12.3")
  implementation("com.guseyn.broken-xml:broken-xml:1.0.21")
  implementation("com.rometools:rome:1.18.0")
  implementation("com.rometools:rome-modules:1.16.0")
  implementation("org.jsoup:jsoup:1.15.3")
  implementation("us.codecraft:xsoup:0.3.2")
  implementation("com.google.code.gson:gson:2.8.9")

  // https://github.com/shyiko/skedule
  implementation("com.github.shyiko.skedule:skedule:0.4.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  implementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.testcontainers:postgresql:1.19.0")
  testImplementation("org.testcontainers:testcontainers:1.19.0")
  testImplementation("org.testcontainers:junit-jupiter:1.19.0")

//  implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.0.4")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
  this.archiveFileName.set("app.${archiveExtension.get()}")
}

// https://netflix.github.io/dgs/generating-code-from-schema/
val graphqlCodegen = tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
  schemaPaths = mutableListOf(
    "$projectDir/src/main/resources/schema/schema.graphqls"
  )
  packageName = "org.migor.feedless.generated"
  generateInterfaces = false
  language = "java"
}

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
  useJUnitPlatform()
}

val fetchGithubJars = tasks.register("fetchGithubJars", Exec::class) {
  commandLine("sh", "./fetchGithubJars.sh")
}
tasks.getByName("compileKotlin").dependsOn(fetchGithubJars, codegen)

tasks.getByName("compileTestKotlin").dependsOn(codegen)

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

val testDocker = tasks.register("testDocker", Exec::class) {
  val gitHash = grgit.head().id
  commandLine(
    "sh", "./test/test-docker.sh", gitHash
  )
}

val dockerAmdBuild = tasks.register("buildAmdDockerImage", Exec::class) {
  dependsOn(lintTask, "test", "bootJar")
  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")
  val gitHash = grgit.head().id

  environment("DOCKER_CLI_EXPERIMENTAL", "enabled")
  commandLine(
    "docker", "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "--build-arg", "APP_BUILD_TIMESTAMP=${Date().time}",
    "--platform=linux/amd64",
//    "-t", "$baseTag:core",
    "-t", "$baseTag:core-$gitHash",
    "."
  )
}

val dockerBuild = tasks.register("buildDockerImage") {
  dependsOn(dockerAmdBuild)
  finalizedBy(testDocker)
}
