import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "3.0.3"
  id("com.adarshr.test-logger") version "3.2.0"
  id("com.netflix.dgs.codegen") version "5.6.9"
//   https://github.com/google/protobuf-gradle-plugin
  id("org.ajoberstar.grgit") version "5.0.0"
  id("com.google.protobuf") version "0.9.2"
  kotlin("jvm") version "1.8.10"
  kotlin("plugin.spring") version "1.8.10"
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
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

val versions = mapOf(
  "kotlinxCoroutines" to "1.6.0",
//  "grpc" to "1.53.0",
  "dgs" to "6.0.1"
)

dependencies {
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["kotlinxCoroutines"]}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${versions["kotlinxCoroutines"]}")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework:spring-aspects")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.apache.tika:tika-core:2.4.1")
  implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.5.0")
  implementation("org.redundent:kotlin-xml-builder:1.7.4")
  // https://mvnrepository.com/artifact/org.apache.commons/commons-text
  implementation("org.apache.commons:commons-text:1.10.0")

  // graphql
//  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation("org.springframework.boot:spring-boot-starter-websocket")
  implementation("org.springframework.security:spring-security-messaging")
  implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${versions["dgs"]}"))
  implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:${versions["dgs"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars:${versions["dgs"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets:${versions["dgs"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets-autoconfigure:${versions["dgs"]}")

//  implementation("com.google.firebase:firebase-messaging:23.1.1")

  // cache
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.ehcache:ehcache:3.10.8")

  // monitoring
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-mail")
  // https://github.com/micrometer-metrics/micrometer
  implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
  implementation("com.github.loki4j:loki-logback-appender:1.3.2")

  // grpc
//  implementation("io.grpc:grpc-netty:${versions["grpc"]}")
//  implementation("io.grpc:grpc-protobuf:${versions["grpc"]}")
//  implementation("io.grpc:grpc-stub:${versions["grpc"]}")

  // security
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  // json feed
  implementation("org.json:json:20220924")
  implementation("com.google.guava:guava:31.1-jre")

  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("commons-io:commons-io:2.11.0")

  // reactor
  // https://mvnrepository.com/artifact/io.projectreactor/reactor-core
  implementation("io.projectreactor:reactor-core:3.5.0")
//  implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")

  // elastic search
  implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

  // database
  implementation("org.postgresql:postgresql:42.5.1")
//  testImplementation("com.h2database:h2:2.1.214")
  implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
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
  testImplementation("org.testcontainers:postgresql:1.18.3")
//  testImplementation("com.h2database:h2:2.0.214")

//  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.7.1")
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
  dependsOn(graphqlCodegen, compilejj)
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
val compilejj = tasks.register("compilejj", Exec::class) {
  inputs.files(fileTree("src/templates"))
    .withPropertyName("sourceFiles")
    .withPathSensitivity(PathSensitivity.RELATIVE)
  commandLine("sh", "./compilejj.sh")
}
val cleanjj = tasks.register("cleanjj", Exec::class) {
  commandLine("sh", "./cleanjj.sh")
}
tasks.getByName("compileKotlin").dependsOn(fetchGithubJars, codegen)

tasks.getByName("compileTestKotlin").dependsOn(compilejj, codegen)
tasks.getByName("clean").dependsOn(cleanjj)

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
  args("--spring.profiles.active=dev")
}

val lintTask = tasks.register("lint") {
//  todo mag
//  dependsOn("lintDockerImage")
}
tasks.register("start") {
  dependsOn("codegen", "bootRun")
}

//val appBuild = tasks.findByPath(":packages:app:build")
//
//val copyAppDist = tasks.register<Copy>("copyAdminDist") {
//  dependsOn(appBuild)
//  from(appBuild!!.outputs.files)
//  into("${project.buildDir}/app")
//  println("Copied to ${project.buildDir}/app");
//}

val testDocker = tasks.register("testDocker", Exec::class) {
  commandLine(
    "sh", "./test/test-docker.sh"
  )
}

val dockerAmdBuild = tasks.register("buildAmdDockerImage", Exec::class) {
  dependsOn(lintTask, "test", "bootJar")
  val major = findProperty("majorVersion") as String
  val coreVersion = findProperty("coreVersion") as String
  val majorMinorPatch = "$major.$coreVersion"
  val majorMinor = "$major.${coreVersion.split(".")[0]}"

  val imageName = "${findProperty("dockerImageTag")}:core"
  val gitHash = grgit.head().abbreviatedId

  environment("DOCKER_CLI_EXPERIMENTAL", "enabled")
  commandLine(
    "docker", "build",
    "--build-arg", "APP_CORE_VERSION=$majorMinorPatch",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "--platform=linux/amd64",
    "-t", "$imageName-$majorMinorPatch",
    "-t", "$imageName-$majorMinor",
    "-t", "$imageName-$major",
    "-t", imageName,
    "."
  )
}

val dockerArmBuild = tasks.register("buildArmDockerImage", Exec::class) {
  dependsOn(lintTask, "test", "bootJar")
  val major = findProperty("majorVersion") as String
  val coreVersion = findProperty("coreVersion") as String
  val majorMinorPatch = "$major.$coreVersion"
  val majorMinor = "$major.${coreVersion.split(".")[0]}"

  val imageName = "${findProperty("dockerImageTag")}:core"
  val gitHash = grgit.head().abbreviatedId

  // docker buildx setup https://stackoverflow.com/a/70837025
  /*
  - docker runtime >= 19.03
  - export DOCKER_CLI_EXPERIMENTAL=enabled
  - docker run --rm --privileged docker/binfmt:66f9012c56a8316f9244ffd7622d7c21c1f6f28d
  - docker buildx create --use --name multi-arch-builder
  - docker buildx ls
   */
  environment("DOCKER_CLI_EXPERIMENTAL", "enabled")
  commandLine(
    "docker", "buildx", "build",
    "--build-arg", "APP_CORE_VERSION=$majorMinorPatch",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "--platform=linux/arm64",
//    "--platform=linux/arm64,linux/amd64",
    "-t", "$imageName-$majorMinorPatch-arm",
    "-t", "$imageName-$majorMinor-arm",
    "-t", "$imageName-$major-arm",
    "-t", "$imageName-arm",
    "--load",
    "."
  )
}

val dockerBuild = tasks.register("buildDockerImage") {
  dependsOn(dockerAmdBuild)
  finalizedBy(testDocker)
}
