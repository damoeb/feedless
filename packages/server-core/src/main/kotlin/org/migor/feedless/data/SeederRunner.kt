package org.migor.feedless.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.migor.feedless.AppProfiles
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
@Profile(AppProfiles.seed)
class SeederRunner(
  private val seeder: Seeder
) : ApplicationListener<ContextRefreshedEvent> {

  override fun onApplicationEvent(event: ContextRefreshedEvent) {
    CoroutineScope(Dispatchers.Default).launch {
      seeder.seed()
    }
  }
}
