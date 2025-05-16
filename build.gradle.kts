import java.util.*

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
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "--build-arg", "APP_BUILD_TIMESTAMP=${Date().time}",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$baseTag:aio-latest",
    "-t", "$baseTag:aio-$gitHash",
    "docker/aio-with-web"
  )
}

val buildImages = tasks.register("buildImages") {
  dependsOn(
    appWebDockerImageTask(),
    serverCoreDockerImageTask(),
    agentDockerImageTask(),
  )
}

val stopServices = tasks.register("stopServices", Exec::class) {
  dependsOn(buildImages)
  commandLine("docker-compose", "stop", "feedless-app", "feedless-agent", "feedless-core")
}
val cleanServices = tasks.register("cleanServices", Exec::class) {
  dependsOn(stopServices)
  commandLine("docker-compose", "rm", "-f", "feedless-app", "feedless-agent", "feedless-core")
}
tasks.register("startServices", Exec::class) {
  dependsOn(
    buildImages,
    stopServices,
    cleanServices
  )
  commandLine(
    "${podmanOrDocker()}-compose",
    "up",
    "--detach",
    "feedless-app",
    "feedless-agent",
    "feedless-core"
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
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "--build-arg", "APP_BUILD_TIMESTAMP=${Date().time}",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$baseTag:aio-chromium-latest",
    "-t", "$baseTag:aio-chromium-$gitHash",
    "docker/aio-with-chromium"
  )
}


//tasks.register("publish", Exec::class) {
////  dependsOn(buildTask)
//
//  val gitHash = grgit.head().id
//  val semver = (findProperty("feedlessVersion") as String).split(".")
//  val major = semver[0]
//  val minor = semver[1]
//  val patch = semver[2]
//  commandLine("sh", "./scripts/semver-tag-docker-images.sh", gitHash, major, minor, patch)
//}

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
