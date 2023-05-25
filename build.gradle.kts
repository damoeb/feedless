buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath ("com.github.node-gradle:gradle-node-plugin:${findProperty("gradleNodePluginVersion")}")
  }
}

val buildDockerAioWeb = tasks.register("buildDockerAio", Exec::class) {
  val appWeb = tasks.findByPath("packages:app-web:buildDockerImage")
  val core = tasks.findByPath("packages:server-core:buildDockerImage")
  val agent = tasks.findByPath("packages:agent:buildDockerImage")
  dependsOn(appWeb, core, agent)

  // with web

  val major = findProperty("majorVersion") as String
  val appVersion = "$major.${findProperty("uiVersion") as String}"
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

  // with chrome
  val agentVersion = "$major.${findProperty("agentVersion") as String}"
  val tagBundlePuppeteer = "aio-chrome"
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
