import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id("com.github.node-gradle.node")
  id("org.ajoberstar.grgit")
}
// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion: String by project
  version.set(nodejsVersion)
  download.set(true)
}

val prepareTask = tasks.register("prepare") {
  dependsOn(codegenTask, yarnInstallTask)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val codegenTask = tasks.register<YarnTask>("codegen") {
  args.set(listOf("codegen"))
  dependsOn(yarnInstallTask)

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.files("../server-core/src/main/resources/schema/schema.graphqls", "generate-verticals-data.ts")

  outputs.files(
    "build/generate-verticals-data.js",
    "src/generated/graphql.ts",
    "all-verticals.json",
    "build/generate-verticals-data.js"
  )
  outputs.dir("build/generated")
}

val lintTask = tasks.register<YarnTask>("lint") {
  dependsOn(prepareTask)

  args.set(listOf("lint"))

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.dir("src")
  inputs.files(
    "angular.json",
    "yarn.lock",
    ".prettierignore",
    ".prettierrc.json",
    "tsconfig.json",
    "tsconfig.app.json",
    "tsconfig.spec.json",
    "tslint.json"
  )
  outputs.upToDateWhen { true }
}

val testTask = tasks.register<YarnTask>("test") {
  dependsOn(prepareTask)
  args.set(listOf("test:ci"))

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.dir("src")
  inputs.dir("node_modules")
  inputs.files(
    "angular.json", ".browserslistrc", "tsconfig.json", "tsconfig.app.json", "tsconfig.spec.json",
    "tslint.json"
  )
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<YarnTask>("build") {
  dependsOn(prepareTask, lintTask, testTask, codegenTask)
  args.set(listOf("build:prod"))

  inputs.property("nodejsVersion", findProperty("nodejsVersion"))
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("www")
}

tasks.register("bundle", Exec::class) {
  dependsOn(buildTask)
  val semver = findProperty("feedlessVersion") as String
  val gitHash = grgit.head().id.take(7)
  val baseTag = findProperty("dockerImageTag")

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)

  commandLine(
    podmanOrDocker(), "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_HASH=$gitHash",
    "-t", "$baseTag:app-latest",
    "-t", "$baseTag:app-$gitHash",
    "."
  )
}

tasks.register("clean", Exec::class) {
  commandLine(
    "rm", "-rf",
    "www", "build",
  )
}

tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(yarnInstallTask, codegenTask)
}

fun podmanOrDocker(): String {
  val env = "DOCKER_BIN"
  val podmanOrDocker = System.getenv(env) ?: "docker"

  println("Using DOCKER_BIN $podmanOrDocker")
  return podmanOrDocker
}
