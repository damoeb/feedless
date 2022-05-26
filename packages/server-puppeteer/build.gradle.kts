import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id ("com.github.node-gradle.node")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion: String by project
  version.set(nodejsVersion)
  npmVersion.set("")
  yarnVersion.set("")
  download.set(false)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val lintTask = tasks.register<YarnTask>("lint") {
  args.set(listOf("lint"))
  dependsOn(yarnInstallTask)
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val testTask = tasks.register<YarnTask>("test") {
  args.set(listOf("test"))
  dependsOn(yarnInstallTask)
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(yarnInstallTask, lintTask, testTask)
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("dist")
}

tasks.register("buildDockerImage", Exec::class) {
  dependsOn(buildTask)
  val majorMinorPatch = findProperty("richVersion") as String
  val versionParts = majorMinorPatch.split(".")
  val majorMinor = versionParts.slice(0..1).joinToString(".")
  val major = versionParts[0]

  val imageName = "${findProperty("dockerImageTag")}:puppeteer"

  commandLine("docker", "build",
    "-t", "${imageName}-${majorMinorPatch}",
    "-t", "${imageName}-${majorMinor}",
    "-t", "${imageName}-${major}",
    "-t", imageName,
    ".")
}


tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(yarnInstallTask)
}
