package org.migor.feedless.systemSettings

interface SystemSettingsRepository {
  fun findByName(name: String): SystemSettings?
  fun save(systemSettings: SystemSettings): SystemSettings
}
