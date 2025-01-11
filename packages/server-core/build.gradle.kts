import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

val versions = mapOf(
  "kotlinxCoroutines" to "1.7.2",
  "dgs-framework" to "8.7.1",
  "testcontainers" to "1.20.1",
)

plugins {
  // https://github.com/Netflix/dgs-framework/blob/v8.7.1/graphql-dgs-client/dependencies.lock
  id("org.springframework.boot") version "3.2.5"
  id("com.netflix.dgs.codegen") version "6.3.0"

  id("com.adarshr.test-logger") version "3.2.0"
  id("org.ajoberstar.grgit")
  id("jacoco")
  id("org.javacc.javacc") version "3.0.2"
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
  java.srcDir("src/generated/java")
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
    val process = ProcessBuilder("id", "-u").start()
    val uid = process.inputStream.bufferedReader().readText().trim()
    environment("DOCKER_HOST", "unix:///run/user/$uid/podman/podman.sock")
  } else {
    throw IllegalArgumentException("test currently only run on linux")
  }
//  } else if (os.isMacOsX) {
//    environment("DOCKER_HOST", "unix:///tmp/podman.sock")
//  }
  environment("TESTCONTAINERS_RYUK_DISABLED", "true")
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
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["kotlinxCoroutines"]}")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions["kotlinxCoroutines"]}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${versions["kotlinxCoroutines"]}")
//  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${versions["kotlinxCoroutines"]}")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework:spring-aspects")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-freemarker")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation("org.apache.tika:tika-core:2.9.0")
  implementation("org.apache.pdfbox:pdfbox-tools:2.0.29")
  implementation("net.sf.cssbox:pdf2dom:2.0.3")
  implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.5.0")
//  implementation("org.redundent:kotlin-xml-builder:1.7.4")
  // https://mvnrepository.com/artifact/org.apache.commons/commons-text
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("org.sejda.webp-imageio:webp-imageio-sejda:0.1.0")


  // graphql
//  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation("org.springframework.boot:spring-boot-starter-websocket")
  implementation("org.springframework.security:spring-security-messaging")
  implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${versions["dgs-framework"]}"))
  implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:${versions["dgs-framework"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars:${versions["dgs-framework"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets:${versions["dgs-framework"]}")
  implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets-autoconfigure:${versions["dgs-framework"]}")
  testImplementation("org.springframework.graphql:spring-graphql-test:1.2.3")
//  implementation("org.mapstruct:mapstruct:1.5.5.Final")
//  annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

  // cache
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.ehcache:ehcache:3.10.8")

  // monitoring
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  // mail
  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("com.mailgun:mailgun-java:1.1.3")
  // https://github.com/micrometer-metrics/micrometer
  implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
//  implementation("com.github.loki4j:loki-logback-appender:1.3.2")

  // security
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  // https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
  implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")

  // json feed
  implementation("org.json:json:20240303")
  implementation("com.google.guava:guava:33.1.0-jre")
// https://mvnrepository.com/artifact/org.mnode.ical4j/ical4j
  implementation("org.mnode.ical4j:ical4j:4.0.5")

  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("commons-io:commons-io:2.11.0")

  // readability
  implementation("net.dankito.readability4j:readability4j:1.0.8")

  // reactor
  // https://mvnrepository.com/artifact/io.projectreactor/reactor-core
  implementation("io.projectreactor:reactor-core:3.5.0")
  implementation("io.projectreactor:reactor-test:3.5.0")

  // database
  implementation("org.postgresql:postgresql:42.7.4")
  implementation("org.hibernate.orm:hibernate-spatial:6.4.10.Final")
  // https://kotlin-jdsl.gitbook.io/docs/jpql-with-kotlin-jdsl/expressions
  implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.1")
  implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.1")
  implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.1")

  // text
  // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analysis-common
  implementation("org.apache.lucene:lucene-analysis-common:9.10.0")
  implementation("org.apache.opennlp:opennlp-tools:2.3.3")
  implementation("org.apache.commons:commons-math3:3.6.1")
  testImplementation("io.jenetics:jenetics:7.2.0")
  implementation("org.languagetool:language-en:6.4")
  implementation("org.languagetool:language-de:6.4")

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
  testCompileOnly("org.junit.jupiter:junit-jupiter-params")
  implementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.testcontainers:postgresql:${versions["testcontainers"]}")
  testImplementation("org.testcontainers:testcontainers:${versions["testcontainers"]}")
  testImplementation("org.testcontainers:junit-jupiter:${versions["testcontainers"]}")
// Property-Based-Testing https://mvnrepository.com/artifact/net.jqwik/jqwik
  testImplementation("net.jqwik:jqwik:1.9.0")


//  testImplementation("org.powermock:powermock-api-mockito:2.0.9")
//  testImplementation("org.powermock:powermock-module-junit4:2.0.9")


  implementation("org.telegram:telegrambots-spring-boot-starter:6.1.0")

  // payments
  implementation("com.stripe:stripe-java:25.0.0")
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

val testDocker = tasks.register("testDocker", Exec::class) {
  val gitHash = grgit.head().id.take(7)
  commandLine(
    "sh", "./test/test-docker.sh", gitHash
  )
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
//    "-t", "$baseTag:core",
    "-t", "$baseTag:core-latest",
    "-t", "$baseTag:core-$gitHash",
    "."
  )
}

tasks.register("bundle") {
  dependsOn(dockerAmdBuild)
  finalizedBy(testDocker)
}

fun podmanOrDocker(): String {
  val env = "DOCKER_BIN"
  val podmanOrDocker = System.getenv(env) ?: "podman"

  println("Using DOCKER_BIN $podmanOrDocker")
  return podmanOrDocker
}
