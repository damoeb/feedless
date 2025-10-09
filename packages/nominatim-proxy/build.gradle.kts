import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id("com.github.node-gradle.node")
  id("org.ajoberstar.grgit")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nvmrcFile = file(".nvmrc")
  val nodeVersion = if (nvmrcFile.exists()) {
    nvmrcFile.readText().trim()
  } else {
    throw IllegalStateException(".nvmrc file not found")
  }
  version.set(nodeVersion)
  download.set(true)
}

val yarnCleanTask = tasks.register<YarnTask>("yarnClean") {
  dependsOn(yarnInstallTask)
  args.set(listOf("clean"))
}

val gradleCleanTask = tasks.register<Delete>("clean") {
  dependsOn(yarnCleanTask)
  delete(project.buildDir)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))

  inputs.file(".nvmrc")
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val prepareTask = tasks.register("prepare") {
  dependsOn(yarnInstallTask)
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(prepareTask)

  inputs.file(".nvmrc")
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.files("yarn.lock", "tsconfig.json")
  outputs.dir("dist")
}

tasks.register("bundle", Exec::class) {
  dependsOn(buildTask)
  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")

  val gitHash = grgit.head().id.take(7)

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)
  inputs.property("semver", semver)

  commandLine(
    "docker", "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "-t", "nominatim-proxy-latest",
    "-t", "nominatim-proxy-$gitHash",
    "."
  )
}
