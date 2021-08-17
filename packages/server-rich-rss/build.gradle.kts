import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
}

group = "org.migor.rich.rss"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

apply(plugin = "java")
apply(plugin = "idea")
apply(plugin = "kotlin")
apply(plugin = "kotlin-spring")

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

  // json feed
  implementation(files("libs/pertwee-1.1.0.jar"))
  implementation("org.json:json:20201115")
  implementation("com.google.guava:guava:28.2-jre")

  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("commons-io:commons-io:2.8.0")

//  implementation("org.postgresql:postgresql:42.2.18")
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

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

task("fetchGithubJars", Exec::class) {
  commandLine("sh", "./fetchGithubJars.sh")
}
tasks.getByName("compileKotlin").dependsOn("fetchGithubJars")

task("compilejjFilterExpr", Exec::class) {
  inputs.files(fileTree("src/templates"))
    .withPropertyName("sourceFiles")
    .withPathSensitivity(PathSensitivity.RELATIVE)
  commandLine ("sh", "./compilejj.sh")
}
task("cleanjjFilterExpr", Exec::class) {
  commandLine ("sh", "./cleanjj.sh")
}
tasks.getByName("compileKotlin").dependsOn("compilejjFilterExpr")
tasks.getByName("compileTestKotlin").dependsOn("compilejjFilterExpr")
tasks.getByName("clean").dependsOn("cleanjjFilterExpr")

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
  args("--spring.profiles.active=dev")
}

tasks.register("buildDockerImage", Exec::class) {
  dependsOn("test", "bootJar")
  commandLine("docker", "build", "-t", "rich-rss:rss", ".")
}
