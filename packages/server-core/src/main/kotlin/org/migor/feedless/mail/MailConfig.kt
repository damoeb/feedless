package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(AppProfiles.mail)
class MailConfig
