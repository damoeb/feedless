package org.migor.feedless.license

/**
 * `APP_BUILD_TIMESTAMP` as epoch milliseconds — the build date both GraphQL `serverSettings` (via
 * [LicenseUseCase.getBuildDate]) and `GET /api/v1/status` report, parsed in this one place.
 */
fun parseBuildTimestamp(raw: String?): Long =
  raw?.toLongOrNull() ?: throw IllegalArgumentException("Invalid properties. APP_BUILD_TIMESTAMP expected, found '$raw'")
