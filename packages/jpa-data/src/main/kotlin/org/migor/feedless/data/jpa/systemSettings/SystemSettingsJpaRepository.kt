package org.migor.feedless.data.jpa.systemSettings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.systemSettings.SystemSettings
import org.migor.feedless.systemSettings.SystemSettingsRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(AppLayer.repository)
class SystemSettingsJpaRepository(private val systemSettingsDAO: SystemSettingsDAO) : SystemSettingsRepository {
  override suspend fun findByName(name: String): SystemSettings? {
    return withContext(Dispatchers.IO) {
      systemSettingsDAO.findByName(name)?.toDomain()
    }
  }

  override suspend fun save(systemSettings: SystemSettings): SystemSettings {
    return withContext(Dispatchers.IO) {
      systemSettingsDAO.save(systemSettings.toEntity()).toDomain()
    }
  }

}
