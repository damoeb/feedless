buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath ("com.github.node-gradle:gradle-node-plugin:${findProperty("gradleNodePluginVersion")}")
  }
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
    "feedless-app",
    "feedless-agent",
    "feedless-core"
  )
  finalizedBy(waitForContainers)
}

//val stopCoreContainer = tasks.register("stopCoreContainer", Exec::class) {
//  commandLine(
//    "docker-compose",
//    "stop",
//    "feedless-core"
//  )
//  doLast {
//    println("core stopped")
//  }
//}

//tasks.register("startCoreContainer", Exec::class) {
//  dependsOn(stopCoreContainer)
//  commandLine(
//    "docker-compose",
//    "up",
//    "-d",
//    "feedless-core"
//  )
//  finalizedBy(waitForContainers)
//}

tasks.register("stopContainers", Exec::class) {
  commandLine(
    "docker-compose",
    "stop",
    "postgres",
    "feedless-app",
    "feedless-agent",
    "feedless-core"
  )
}

val buildDockerAioWeb = tasks.register("buildDockerAio", Exec::class) {
  val appWeb = tasks.findByPath("packages:app-web:buildDockerImage")
  val core = tasks.findByPath("packages:server-core:buildDockerImage")
  val agent = tasks.findByPath("packages:agent:buildDockerImage")
  dependsOn(appWeb, core, agent)

  // with web

  val major = findProperty("majorVersion") as String
  val appVersion = "$major.${findProperty("appVersion") as String}"
  val coreVersion = "$major.${findProperty("coreVersion") as String}"
  val bundleVersion = "$major.${findProperty("bundleVersion") as String}"

  val tagBundleWeb = "aio"
  val webImageName = "${findProperty("dockerImageTag")}:$tagBundleWeb"

  commandLine(
    "docker", "build",
    "--build-arg", "APP_APP_VERSION=$appVersion",
    "--build-arg", "APP_CORE_VERSION=$coreVersion",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$webImageName-$bundleVersion",
    "-t", webImageName,
    "docker-images/with-web"
  )

  // with chromium
  val agentVersion = "$major.${findProperty("agentVersion") as String}"
  val tagBundlePuppeteer = "aio-chromium"
  val puppeteerImageName = "${findProperty("dockerImageTag")}:$tagBundlePuppeteer"
  commandLine(
    "docker", "build",
    "--build-arg", "APP_AGENT_VERSION=$agentVersion",
    "--build-arg", "APP_CORE_VERSION=$coreVersion",
    "--build-arg", "APP_TAG_BUNDLE_WEB=$tagBundleWeb",
    "--build-arg", "APP_BUNDLE_VERSION=$bundleVersion",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$puppeteerImageName-$bundleVersion",
    "-t", puppeteerImageName,
    "docker-images/with-puppeteer"
  )
}

subprojects {
  tasks.register("lintDockerImage", Exec::class) {
    commandLine("sh", rootProject.file("lintDockerfile.sh").getAbsolutePath(), project.file("Dockerfile").getAbsolutePath())
  }
}
