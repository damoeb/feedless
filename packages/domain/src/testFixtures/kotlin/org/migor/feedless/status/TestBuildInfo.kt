package org.migor.feedless.status

fun testBuildInfo(
  version: String = "test",
  commit: String = "unknown",
  timestamp: String = "0",
) = BuildInfo(version = version, commit = commit, timestamp = timestamp)
