package org.migor.rss.rich.services

import org.migor.rss.rich.models.DownloadTask
import org.migor.rss.rich.repositories.DownloadTaskRepository
import org.migor.rss.rich.repositories.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SubscriptionService {

  @Autowired
  lateinit var repository: SubscriptionRepository

}
