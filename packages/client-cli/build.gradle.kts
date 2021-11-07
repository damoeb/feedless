plugins {
	kotlin("jvm") version "1.5.21"
  id("io.github.ermadmi78.kobby") version "1.3.0"
}

group = "org.migor.rss.rich.cli"
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
// Add this dependency to enable Jackson annotation generation in DTO classes
compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.12.2")

// Add this dependency to enable default Ktor adapters generation
compileOnly("io.ktor:ktor-client-cio:1.5.4")
}

//tasks.register("codegen") {
//  dependsOn("graphqlCodegen")
//}

tasks.named<JavaCompile>("compileJava") {
//  dependsOn("graphqlCodegen")
}

// todo mag https://medium.com/@ermadmi78/how-to-generate-kotlin-dsl-client-by-graphql-schema-707fd0c55284
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val lintTask = tasks.register("lint") {
  dependsOn("lintDockerImage")
}
tasks.register("buildDockerImage", Exec::class) {
//  dependsOn(lintTask, "test", "bootJar")
//  commandLine("docker", "build", "-t", "rich-rss:rss-kotlin", ".")
}

tasks.register("start") {

}
