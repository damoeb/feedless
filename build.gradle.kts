import java.util.*

buildscript {
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("org.ajoberstar.grgit")
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.spring) apply false
  alias(libs.plugins.kapt) apply false
}

val buildDockerAioWeb = tasks.register("buildDockerAioWeb", Exec::class) {
  dependsOn(appWebDockerImageTask(), serverCoreDockerImageTask(), agentDockerImageTask())

  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")
  val gitHash = grgit.head().id.take(7)

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)
  inputs.property("semver", semver)

  // with web
  commandLine(
    podmanOrDocker(), "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_COMMIT=$gitHash",
    "--build-arg", "APP_BUILD_TIMESTAMP=${Date().time}",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$baseTag:aio-latest",
    "-t", "$baseTag:aio-$gitHash",
    "docker/aio-with-web"
  )
}

val validateAgentServerCanConnect = tasks.register("validateAgentServerCanConnect", Exec::class) {
  commandLine(
    "./test/system/validate-agent-server-can-connect.sh"
  )
}

val buildImages = tasks.register("buildImages") {
// todo enable finalizedBy(validateAgentServerCanConnect)
  dependsOn(
    appWebDockerImageTask(),
    serverCoreDockerImageTask(),
    agentDockerImageTask(),
  )
}

val buildDockerAioChromium = tasks.register("buildDockerAioChromium", Exec::class) {
  dependsOn(buildDockerAioWeb)

  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")
  val gitHash = grgit.head().id.take(7)

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)
  inputs.property("semver", semver)

  // with chromium
  commandLine(
    podmanOrDocker(), "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_COMMIT=$gitHash",
    "--build-arg", "APP_BUILD_TIMESTAMP=${Date().time}",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$baseTag:aio-chromium-latest",
    "-t", "$baseTag:aio-chromium-$gitHash",
    "docker/aio-with-chromium"
  )
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

fun appWebDockerImageTask() = tasks.findByPath("packages:app-web:bundle")
fun serverCoreDockerImageTask() = tasks.findByPath("packages:server-core:bundle")
fun agentDockerImageTask() = tasks.findByPath("packages:agent:bundle")

fun podmanOrDocker(): String {
  val env = "DOCKER_BIN"
  val podmanOrDocker = System.getenv(env) ?: "docker"

  println("Using DOCKER_BIN $podmanOrDocker")
  return podmanOrDocker
}
