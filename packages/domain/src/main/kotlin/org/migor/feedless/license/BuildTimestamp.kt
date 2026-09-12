package org.migor.feedless.license

/** Parsed in one place for both GraphQL serverSettings and GET /api/v1/status. */
fun parseBuildTimestamp(raw: String?): Long =
  raw?.toLongOrNull() ?: throw IllegalArgumentException("Invalid properties. APP_BUILD_TIMESTAMP expected, found '$raw'")
