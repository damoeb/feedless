package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!${AppProfiles.mail}")
class MockMailService: MailService {
  private val log = LoggerFactory.getLogger(MockMailService::class.simpleName)

  override fun send(mail: String, subject: String, text: String) {
    log.info("send mail $text")
  }
}
