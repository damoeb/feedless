buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath("com.github.node-gradle:gradle-node-plugin:${findProperty("gradleNodePluginVersion")}")
  }
}

plugins {
  id("org.ajoberstar.grgit")
}

val waitForContainers = tasks.register("WaitForContainers", Exec::class) {
  commandLine(
    "sh",
    "scripts/wait-for-containers.sh"
  )
}

tasks.register("startContainers", Exec::class) {
  commandLine(
    "docker-compose",
    "up",
    "-d",
    "postgres",
//    "feed-validator",
    "feedless-app",
    "feedless-agent",
    "feedless-core"
  )
  finalizedBy(waitForContainers)
}

tasks.register("stopContainers", Exec::class) {
  commandLine(
    "docker-compose",
    "stop",
//    "postgres",
//    "feed-validator",
    "feedless-app",
    "feedless-agent",
    "feedless-core"
  )
}

val buildDockerAioWeb = tasks.register("buildDockerAioWeb", Exec::class) {
  dependsOn(appWebTask(), serverCoreTask(), agentTask())

  val baseTag = findProperty("dockerImageTag")
  val gitHash = grgit.head().id

  // with web
  commandLine(
    "docker", "build",
    "--build-arg", "APP_VERSION=$gitHash",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$baseTag:aio",
    "-t", "$baseTag:aio-$gitHash",
    "docker-images/with-web"
  )
}

val buildDockerAioChromium = tasks.register("buildDockerAioChromium", Exec::class) {
  dependsOn(buildDockerAioWeb)

  val baseTag = findProperty("dockerImageTag")
  val gitHash = grgit.head().id

  // with chromium
  commandLine(
    "docker", "build",
    "--build-arg", "APP_VERSION=$gitHash",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$baseTag:aio-chromium",
    "-t", "$baseTag:aio-chromium-$gitHash",
    "docker-images/with-chromium"
  )
}

val packageAll = tasks.register("package") {
  dependsOn(appWebTask(), serverCoreTask(), agentTask(), buildDockerAioWeb, buildDockerAioChromium)
}

tasks.register("publish", Exec::class) {
  dependsOn(packageAll)

  val gitHash = grgit.head().id
  val semver = (findProperty("feedlessVersion") as String).split(".")
  val major = semver[0]
  val minor = semver[1]
  val patch = semver[2]
  commandLine("sh", "./scripts/semver-tag-docker-images.sh", gitHash, major, minor, patch)
}

subprojects {
  tasks.register("lintDockerImage", Exec::class) {
    commandLine(
      "sh",
      rootProject.file("lintDockerfile.sh").getAbsolutePath(),
      project.file("Dockerfile").getAbsolutePath()
    )
  }
}

fun appWebTask() = tasks.findByPath("packages:app-web:buildDockerImage")
fun serverCoreTask() = tasks.findByPath("packages:server-core:buildDockerImage")
fun agentTask() = tasks.findByPath("packages:agent:buildDockerImage")
