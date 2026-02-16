import com.github.gradle.node.npm.task.NpmTask

plugins {
  alias(libs.plugins.node)
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
  println("nodeVersion: $nodeVersion")
  version.set(nodeVersion)
  download.set(true)
}

val prepareTask = tasks.register("prepare") {
  dependsOn(npmInstallTask)
}

val npmInstallTask = tasks.findByName("npmInstall")

val lintTask = tasks.register<NpmTask>("lint") {
  dependsOn(prepareTask)
  args.set(listOf("run", "lint"))
}

val testTask = tasks.register<NpmTask>("test") {
  dependsOn(prepareTask)
  args.set(listOf("run", "test"))
}

val buildTask = tasks.register<NpmTask>("build") {
  dependsOn(prepareTask, lintTask, testTask)
  args.set(listOf("run", "build:prod"))
}

val systemTest = tasks.register("systemTest", Exec::class) {
  commandLine(
    "./test/test-upcoming-container.sh"
  )
}

tasks.register("bundle", Exec::class) {
  dependsOn(buildTask)
  finalizedBy(systemTest)
  val semver = findProperty("feedlessVersion") as String
  val gitHash = grgit.head().id.take(7)
  val baseTag = findProperty("dockerImageTag")

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)

  commandLine(
    "docker", "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_COMMIT=$gitHash",
    "-t", "$baseTag:app-upcoming-latest",
    "-t", "$baseTag:app-upcoming-$gitHash",
    "."
  )
}
