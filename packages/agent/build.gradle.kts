import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id("com.github.node-gradle.node")
  id("org.ajoberstar.grgit")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion = findProperty("nodejsVersion") as String
  version.set(nodejsVersion)
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

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val lintTask = tasks.register<YarnTask>("lint") {
  dependsOn(yarnInstallTask)
  args.set(listOf("lint"))

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val codegenTask = tasks.register<YarnTask>("codegen") {
  args.set(listOf("codegen"))
  dependsOn(yarnInstallTask)

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.dir("src")
  inputs.files("codegen.yml", "yarn.lock", "../server-core/src/main/resources/schema/schema.graphqls")
  outputs.upToDateWhen { true }
}

val prepareTask = tasks.register("prepare") {
  dependsOn(yarnInstallTask, codegenTask)
}

val testTask = tasks.register<YarnTask>("test") {
  args.set(listOf("test"))
  dependsOn(prepareTask)
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(prepareTask, lintTask, testTask)

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("dist")
}

val validateAgentContainer = tasks.register("validateAgentContainer", Exec::class) {
  commandLine(
    "./test/system/validate-agent-container.sh"
  )
}

tasks.register("bundle", Exec::class) {
  dependsOn(buildTask)
  finalizedBy(validateAgentContainer)
  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")

  val gitHash = grgit.head().id.take(7)

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)
  inputs.property("semver", semver)

  commandLine(
    podmanOrDocker(), "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "-t", "$baseTag:agent-latest",
    "-t", "$baseTag:agent-$gitHash",
    "."
  )
}


tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(prepareTask)
}

fun podmanOrDocker(): String {
  val env = "DOCKER_BIN"
  val podmanOrDocker = System.getenv(env) ?: "docker"

  println("Using DOCKER_BIN $podmanOrDocker")
  return podmanOrDocker
}
