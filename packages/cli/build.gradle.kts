// feedctl is a Go module; Gradle shells out to `go` rather than modeling the
// build with a Java/Kotlin plugin (mirrors packages/agent/build.gradle.kts,
// which shells out to yarn).
val openapiSpec = "../http-api/src/main/resources/openapi/openapi.yaml"

// A pre-release (go1.27rc1) gets patch -1, so it sorts before go1.27.0.
data class GoVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<GoVersion> {
  override fun compareTo(other: GoVersion): Int =
    compareValuesBy(this, other, GoVersion::major, GoVersion::minor, GoVersion::patch)

  override fun toString(): String = if (patch < 0) "$major.$minor (pre-release)" else "$major.$minor.$patch"

  companion object {
    private val pattern = Regex("""(\d+)\.(\d+)(?:\.(\d+))?([a-z]+\d*)?""")

    fun parse(text: String): GoVersion? {
      val match = pattern.find(text) ?: return null
      val (major, minor, patch, preRelease) = match.destructured
      return GoVersion(
        major.toInt(),
        minor.toInt(),
        when {
          preRelease.isNotEmpty() && patch.isEmpty() -> -1
          patch.isEmpty() -> 0
          else -> patch.toInt()
        },
      )
    }
  }
}

fun requiredGoVersion(goMod: String): GoVersion {
  val lines = goMod.lines().map { it.trim() }
  val line = lines.firstOrNull { it.startsWith("toolchain ") }
    ?: lines.firstOrNull { it.startsWith("go ") }
    ?: throw GradleException("go.mod has neither a `toolchain` nor a `go` line")
  return GoVersion.parse(line.substringAfter(' '))
    ?: throw GradleException("Cannot parse the Go version in go.mod: `$line`")
}

// `go env GOVERSION` reports the toolchain actually selected, after GOTOOLCHAIN switching.
val checkGoTask = tasks.register("checkGo") {
  val moduleDir = projectDir
  val goMod = file("go.mod")

  doLast {
    val required = requiredGoVersion(goMod.readText())
    val process = try {
      ProcessBuilder("go", "env", "GOVERSION")
        .directory(moduleDir)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    } catch (e: java.io.IOException) {
      throw GradleException(
        "Go not found on PATH. Install Go ≥ 1.21 — it downloads go$required on its own (GOTOOLCHAIN=auto). " +
          "Building the server-core image needs no local Go.",
        e,
      )
    }
    val output = process.inputStream.bufferedReader().readText().trim()
    if (process.waitFor() != 0) {
      throw GradleException("`go env GOVERSION` failed in $moduleDir (exit ${process.exitValue()}); see its output above.")
    }
    val selected = GoVersion.parse(output)
      ?: throw GradleException("Cannot parse the Go version `go env GOVERSION` reported: `$output`")
    if (selected < required) {
      throw GradleException(
        "go.mod requires Go $required, but `go` selected $output. " +
          "Install a newer Go, or unset GOTOOLCHAIN (or set GOTOOLCHAIN=auto) so it downloads go$required itself.",
      )
    }
    logger.info("Go $output satisfies go.mod's $required")
  }
}

val goVetTask = tasks.register<Exec>("goVet") {
  dependsOn(checkGoTask)
  commandLine("go", "vet", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  outputs.upToDateWhen { true }
}

val golangciLintTask = tasks.register<Exec>("golangciLint") {
  dependsOn(checkGoTask)
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
  dependsOn(checkGoTask)
  commandLine("sh", "-c", "go generate ./... && git diff --exit-code -- internal/api")

  inputs.dir("internal/api")
  inputs.file(openapiSpec)
  outputs.upToDateWhen { true }
}

val lintTask = tasks.register("lint") {
  dependsOn(goVetTask, golangciLintTask, generateDriftCheckTask)
}

val testTask = tasks.register<Exec>("test") {
  dependsOn(checkGoTask)
  commandLine("go", "test", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum", "install.sh")
  outputs.upToDateWhen { true }
}

// End-to-end smoke test (e2e/, build tag `e2e`): drives a real feedctl binary
// against a real core, agent and PostGIS started with Testcontainers. Not
// part of `test` — it needs Docker and built images, passed as
// FEEDCTL_E2E_CORE_IMAGE / FEEDCTL_E2E_AGENT_IMAGE (defaults: the published
// damoeb/feedless:core-latest / agent-latest). Skips when Docker is absent.
tasks.register<Exec>("e2eTest") {
  dependsOn(checkGoTask)
  commandLine("go", "test", "-tags", "e2e", "-count=1", "-timeout", "12m", "-v", "./e2e/...")

  outputs.upToDateWhen { false }
}

// Host build only; the binaries served under /cli/** come from server-core's Dockerfile.
val buildTask = tasks.register<Exec>("build") {
  dependsOn(checkGoTask)
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
