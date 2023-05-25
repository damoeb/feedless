buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath ("com.github.node-gradle:gradle-node-plugin:${findProperty("gradleNodePluginVersion")}")
  }
}

val dockerBuild = tasks.register("buildDockerImage", Exec::class) {
//  val appWeb = tasks.findByPath("packages:app-web:buildDockerImage")
//  val agent = tasks.findByPath("packages:agent:buildDockerImage")
//  val core = tasks.findByPath("packages:server-core:buildDockerImage")
//  dependsOn(appWeb, agent, core)

  val major = findProperty("majorVersion") as String
  val appVersion = "$major.${findProperty("uiVersion") as String}"
  val agentVersion = "$major.${findProperty("agentVersion") as String}"
  val coreVersion = "$major.${findProperty("coreVersion") as String}"

  println("appVersion $appVersion")
  println("agentVersion $agentVersion")
  println("coreVersion $coreVersion")
  val imageName = "${findProperty("dockerImageTag")}:all-in-one"

  commandLine(
    "docker", "build",
    "--build-arg", "APP_APP_VERSION=$appVersion",
    "--build-arg", "APP_AGENT_VERSION=$agentVersion",
    "--build-arg", "APP_CORE_VERSION=$coreVersion",
    "--platform=linux/amd64",
//    "--platform=linux/arm64v8",
    "-t", "$imageName-$major",
    "-t", imageName,
    "docker/all-in-one"
  )
}


subprojects {
  tasks.register("lintDockerImage", Exec::class) {
    commandLine("sh", rootProject.file("lintDockerfile.sh").getAbsolutePath(), project.file("Dockerfile").getAbsolutePath())
  }
}
