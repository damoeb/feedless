import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("io.github.kobylynskyi.graphql.codegen") version "5.2.0"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
}

group = "org.migor.rich.rss"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_11
sourceSets.getByName("main") {
  java.srcDir("src/main/java")
  java.srcDir("src/main/kotlin")
  resources.srcDir("src/main/resources")
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

dependencies {
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation ("org.springframework.boot:spring-boot-starter-amqp")
  testImplementation ("org.springframework.amqp:spring-rabbit-test")

  // json feed
  implementation(files("libs/pertwee-1.1.0.jar"))
  implementation("org.json:json:20201115")
  implementation("com.google.guava:guava:28.2-jre")

  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("commons-io:commons-io:2.8.0")

  implementation("mysql:mysql-connector-java:8.0.22")
  implementation("org.asynchttpclient:async-http-client:2.12.1")
  implementation("com.guseyn.broken-xml:broken-xml:1.0.21")
  implementation("com.rometools:rome:1.15.0")
  implementation("com.rometools:rome-modules:1.15.0")
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("us.codecraft:xsoup:0.3.2")
  implementation("com.google.code.gson:gson:2.8.6")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
//  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
  this.archiveFileName.set("app.${archiveExtension.get()}")
}

tasks.named<io.github.kobylynskyi.graphql.codegen.gradle.GraphQLCodegenGradleTask>("graphqlCodegen") {
  // https://github.com/kobylynskyi/graphql-java-codegen/blob/master/docs/codegen-options.md
  graphqlSchemaPaths = listOf("$projectDir/../server-commons/mq-commons.gql")
  outputDir = File("$projectDir/src/main/java")
  packageName = "org.migor.rss.rich.generated"
}

tasks.register("codegen") {
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
tasks.getByName("compileKotlin").dependsOn(fetchGithubJars)

val compilejj = tasks.register("compilejj", Exec::class) {
  inputs.files(fileTree("src/templates"))
    .withPropertyName("sourceFiles")
    .withPathSensitivity(PathSensitivity.RELATIVE)
  commandLine ("sh", "./compilejj.sh")
}
val cleanjj = tasks.register("cleanjj", Exec::class) {
  commandLine ("sh", "./cleanjj.sh")
}
tasks.getByName("compileKotlin").dependsOn(compilejj)
tasks.getByName("compileTestKotlin").dependsOn(compilejj)
tasks.getByName("clean").dependsOn(cleanjj)

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
  args("--spring.profiles.active=dev")
}

val lintTask = tasks.register("lint") {
  dependsOn("lintDockerImage")
}
tasks.register("buildDockerImage", Exec::class) {
  dependsOn(lintTask, "test", "bootJar")
  commandLine("docker", "build", "-t", "rich-rss:rss-kotlin", ".")
}

tasks.register("start") {
  dependsOn("bootRun")
}
