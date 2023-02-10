package org.migor.rich.rss.config

import org.migor.rich.rss.AppProfiles
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(AppProfiles.mail)
class MailConfig
