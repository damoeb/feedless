package org.migor.feedless.systemSettings

interface SystemSettingsRepository {
  suspend fun findByName(name: String): SystemSettings?
  suspend fun save(systemSettings: SystemSettings): SystemSettings
}
