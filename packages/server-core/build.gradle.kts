import graphql.GraphQL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.6.1"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("io.github.kobylynskyi.graphql.codegen") version "5.3.0"
  id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
  id("com.adarshr.test-logger") version "3.2.0"
  kotlin("jvm") version "1.6.10"
  kotlin("plugin.spring") version "1.6.10"
//  id("org.ajoberstar.grgit") version "4.1.0"
}

group = "org.migor.rich.rss"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

//graphql {
//  // https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-maven-plugin/generatePojo-mojo.html
//  packageName = "org.migor.rich.graphql.generated"
//  isCopyRuntimeSources = false
//  isSeparateUtilityClasses = false
//  isGenerateBatchLoaderEnvironment = false
//  isGenerateDataLoaderForLists = false
//  isSkipGenerationIfSchemaHasNotChanged = false
//  mode = com.graphql_java_generator.plugin.conf.PluginMode.server
//}

java.sourceCompatibility = JavaVersion.VERSION_11
sourceSets.getByName("main") {
  java.srcDir("src/main/java")
  java.srcDir("src/main/kotlin")
  resources.srcDir("src/main/resources")
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

val versions = mapOf(
  "kotlinxCoroutines" to "1.6.0"
)

dependencies {
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["kotlinxCoroutines"]}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${versions["kotlinxCoroutines"]}")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-validation")
//  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.springframework.boot:spring-boot-starter-amqp")
  testImplementation("org.springframework.amqp:spring-rabbit-test")
  implementation("org.apache.tika:tika-core:2.4.1")
  implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.5.0")
  implementation("org.redundent:kotlin-xml-builder:1.7.4")

  // graphql
//  implementation("com.graphql-java-kickstart:graphql-spring-boot-starter:14.0.0")
//  implementation("com.graphql-java-kickstart:graphql-java-tools:13.0.1")
  implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release"))
  implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
  implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars:5.4.3")

  // cache
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("javax.cache:cache-api:1.1.1")
  implementation("org.ehcache:ehcache:3.8.1")

  // monitoring
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-mail")
  // https://github.com/micrometer-metrics/micrometer
  implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
  implementation("com.github.loki4j:loki-logback-appender:1.3.2")

  // security
  implementation("com.auth0:java-jwt:3.19.2")
  implementation("org.springframework.boot:spring-boot-starter-security")

  // json feed
  implementation(files("libs/pertwee-1.1.0.jar"))
  implementation("org.json:json:20211205")
  implementation("com.google.guava:guava:31.1-jre")

  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("commons-io:commons-io:2.11.0")

  // database
  implementation("org.postgresql:postgresql:42.5.0")
  implementation("org.hibernate:hibernate-ehcache:5.6.14.Final")

//  implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")
//  implementation("com.h2database:h2:2.1.212")
  implementation("com.vladmihalcea:hibernate-types-52:2.14.0")
//  implementation("org.flywaydb:flyway-core:9.4.0")

  implementation("org.asynchttpclient:async-http-client:2.12.3")
  implementation("com.guseyn.broken-xml:broken-xml:1.0.21")
  implementation("com.rometools:rome:1.18.0")
//  implementation("com.rometools:rome-modules:1.16.0")
  implementation("org.jsoup:jsoup:1.15.3")
  implementation("us.codecraft:xsoup:0.3.2")
  implementation("com.google.code.gson:gson:2.8.9")

  // https://github.com/shyiko/skedule
  implementation("com.github.shyiko.skedule:skedule:0.4.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  implementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("com.h2database:h2:2.0.214")

//  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
  this.archiveFileName.set("app.${archiveExtension.get()}")
}

tasks.named<io.github.kobylynskyi.graphql.codegen.gradle.GraphQLCodegenGradleTask>("graphqlCodegen") {
  // https://github.com/kobylynskyi/graphql-java-codegen/blob/master/docs/codegen-options.md

  graphqlSchemaPaths =
    listOf("$projectDir/../server-commons/mq-commons.gql", "$projectDir/src/main/resources/schema/schema.graphqls")
  outputDir = File("$projectDir/src/main/java")
  packageName = "org.migor.rich.rss.generated"

  modelNameSuffix = "Dto"
  typeResolverSuffix = "Dto"
  apiNameSuffix = "Dto"
  generateApis = false
  generateDataFetchingEnvironmentArgumentInApis = true
  customTypesMapping = mapOf(
    "DateTime" to "java.sql.Timestamp",
//    "JSON" to "",
    "Long" to "java.lang.Long"
  )
}

val codegen = tasks.register("codegen") {
  dependsOn("graphqlCodegen")
}

tasks.named<JavaCompile>("compileJava") {
  dependsOn("graphqlCodegen")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
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
tasks.getByName("compileKotlin").dependsOn(fetchGithubJars, compilejj, codegen)
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

tasks.register("buildDockerImage", Exec::class) {
  dependsOn(lintTask, "test", "bootJar")
  val major = findProperty("majorVersion") as String
  val coreVersion = findProperty("coreVersion") as String
  val majorMinorPatch = "$major.$coreVersion"
  val majorMinor = "$major.${coreVersion.split(".")[0]}"

  val imageName = "${findProperty("dockerImageTag")}:core"
  val gitHash = "1111" //grgit.head().abbreviatedId

  // see https://github.com/docker-library/official-images#multiple-architectures
  // install plarforms https://stackoverflow.com/a/60667468/807017
  // docker buildx ls
//  commandLine("docker", "buildx", "build",
  commandLine(
    "docker", "build",
    "--build-arg", "CORE_VERSION=$majorMinorPatch",
    "--build-arg", "GIT_HASH=$gitHash",
//    "--platform=linux/amd64,linux/arm64",
    "-t", "$imageName-$majorMinorPatch",
    "-t", "$imageName-$majorMinor",
    "-t", "$imageName-$major",
    "-t", imageName,
    "."
  )
}
