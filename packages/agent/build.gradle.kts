import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id ("com.github.node-gradle.node")
  id("org.ajoberstar.grgit")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion = findProperty("nodejsVersion") as String
  version.set(nodejsVersion)
  download.set(true)
}

val libBuild = tasks.findByPath(":packages:client-lib:build")

val copyLibDist = tasks.register<Copy>("copyLibDist") {
  dependsOn(libBuild)
  from(libBuild!!.outputs.files)
  into("${project.buildDir}/client-lib")
  doLast {
    println("Copied to ${project.buildDir}/client-lib")
    println(libBuild.outputs.files.files.toList().joinToString(" "))
  }
}

val libClean = tasks.findByPath(":packages:client-lib:clean")

val yarnCleanTask = tasks.register<YarnTask>("yarnClean") {
  dependsOn(libClean, yarnInstallTask, libBuild)
  args.set(listOf("clean"))
}

val gradleCleanTask = tasks.register<Delete>("clean") {
  dependsOn(yarnCleanTask)
  delete(project.buildDir)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  dependsOn(libBuild)
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

val buildGhosteryTask = tasks.register("buildGhostery", Exec::class) {
  commandLine("sh", "./build-ghostery.sh")
  outputs.dir("ghostery-extension/extension-manifest-v2/dist")
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(yarnInstallTask, lintTask, testTask, buildGhosteryTask)
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("dist")
}

tasks.register("buildDockerImage", Exec::class) {
  dependsOn(buildTask, copyLibDist)
  val semver = (findProperty("feedlessVersion") as String).split(".")
  val baseTag = findProperty("dockerImageTag")

  val gitHash = grgit.head().id

  commandLine("docker", "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "-t", "$baseTag:agent-$gitHash",
    ".")
}


tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(yarnInstallTask)
}
