package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.repositories.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class OpmlService {

  private val log = LoggerFactory.getLogger(OpmlService::class.simpleName)

  @Autowired
  lateinit var userDAO: UserDAO

}
