package org.migor.feedless.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class SeederRunner(
  private val seeder: Seeder
) : ApplicationListener<ContextRefreshedEvent> {

  override fun onApplicationEvent(event: ContextRefreshedEvent) {
    CoroutineScope(Dispatchers.Default).launch {
      seeder.seed()
    }
  }
}
