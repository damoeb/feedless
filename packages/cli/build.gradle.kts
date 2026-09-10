// feedctl is a Go module; Gradle shells out to `go` rather than modeling the
// build with a Java/Kotlin plugin (mirrors packages/agent/build.gradle.kts,
// which shells out to yarn).
val openapiSpec = "../http-api/src/main/resources/openapi/openapi.yaml"

val goVetTask = tasks.register<Exec>("goVet") {
  commandLine("go", "vet", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  outputs.upToDateWhen { true }
}

val golangciLintTask = tasks.register<Exec>("golangciLint") {
  commandLine("go", "tool", "golangci-lint", "run", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum", ".golangci.yml")
  outputs.upToDateWhen { true }
}

// A spec change that nobody regenerated the client for must fail the gate,
// not surface as a silent drift bug later. `go generate` rewrites
// internal/api/client.gen.go from openapiSpec; if that leaves a diff, the
// checked-in client is stale.
val generateDriftCheckTask = tasks.register<Exec>("generateDriftCheck") {
  commandLine("sh", "-c", "go generate ./... && git diff --exit-code -- internal/api")

  inputs.dir("internal/api")
  inputs.file(openapiSpec)
  outputs.upToDateWhen { true }
}

val lintTask = tasks.register("lint") {
  dependsOn(goVetTask, golangciLintTask, generateDriftCheckTask)
}

val testTask = tasks.register<Exec>("test") {
  commandLine("go", "test", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  outputs.upToDateWhen { true }
}

// End-to-end smoke test (e2e/, build tag `e2e`): drives a real feedctl binary
// against a real core, agent and PostGIS started with Testcontainers. Not
// part of `test` — it needs Docker and built images, passed as
// FEEDCTL_E2E_CORE_IMAGE / FEEDCTL_E2E_AGENT_IMAGE (defaults: the published
// damoeb/feedless:core-latest / agent-latest). Skips when Docker is absent.
tasks.register<Exec>("e2eTest") {
  commandLine("go", "test", "-tags", "e2e", "-count=1", "-timeout", "12m", "-v", "./e2e/...")

  outputs.upToDateWhen { false }
}

val buildTask = tasks.register<Exec>("build") {
  val feedlessVersion = (findProperty("feedlessVersion") as String?) ?: "dev"

  commandLine(
    "go", "build",
    "-ldflags", "-X main.version=$feedlessVersion",
    "-o", "build/feedctl",
    "./cmd/feedctl"
  )

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  inputs.property("feedlessVersion", feedlessVersion)
  outputs.file("build/feedctl")
}

tasks.register<Delete>("clean") {
  delete("build")
}

// Each self-hosted instance serves the feedctl build that matches its own
// API (see packages/server-core CliInstallScriptController), so the image
// bundles binaries for every platform we support rather than picking one at
// build time. No Windows build (out of scope).
data class CrossCompileTarget(val os: String, val arch: String) {
  val binaryName = "feedctl-$os-$arch"
}

val crossCompileTargets = listOf(
  CrossCompileTarget("darwin", "amd64"),
  CrossCompileTarget("darwin", "arm64"),
  CrossCompileTarget("linux", "amd64"),
  CrossCompileTarget("linux", "arm64"),
)

val crossCompileTask = tasks.register("crossCompile") {
  val feedlessVersion = (findProperty("feedlessVersion") as String?) ?: "dev"
  val distDir = layout.buildDirectory.dir("dist").get().asFile

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum", "install.sh")
  inputs.property("feedlessVersion", feedlessVersion)
  outputs.dir(distDir)

  doLast {
    delete(distDir)
    distDir.mkdirs()

    crossCompileTargets.forEach { target ->
      project.exec {
        environment("CGO_ENABLED", "0")
        environment("GOOS", target.os)
        environment("GOARCH", target.arch)
        commandLine(
          "go", "build",
          "-trimpath",
          "-ldflags", "-s -w -X main.version=$feedlessVersion",
          "-o", File(distDir, target.binaryName).path,
          "./cmd/feedctl"
        )
      }
    }

    // sha256sum format: "<hex>  <file>", one entry per line.
    val sha256sums = crossCompileTargets.joinToString(separator = "") { target ->
      val digest = java.security.MessageDigest.getInstance("SHA-256")
        .digest(File(distDir, target.binaryName).readBytes())
      val hex = digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
      "$hex  ${target.binaryName}\n"
    }
    File(distDir, "SHA256SUMS").writeText(sha256sums)

    file("install.sh").copyTo(File(distDir, "install.sh"), overwrite = true)
  }
}
