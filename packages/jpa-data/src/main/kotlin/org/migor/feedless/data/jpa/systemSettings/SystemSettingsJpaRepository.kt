package org.migor.feedless.data.jpa.systemSettings

import org.migor.feedless.AppLayer
import org.migor.feedless.systemSettings.SystemSettings
import org.migor.feedless.systemSettings.SystemSettingsRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(AppLayer.repository)
class SystemSettingsJpaRepository(private val systemSettingsDAO: SystemSettingsDAO) : SystemSettingsRepository {
  override fun findByName(name: String): SystemSettings? {
    return systemSettingsDAO.findByName(name)?.toDomain()
  }

  override fun save(systemSettings: SystemSettings): SystemSettings {
    return systemSettingsDAO.save(systemSettings.toEntity()).toDomain()
  }

}
