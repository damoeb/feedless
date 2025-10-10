package org.migor.feedless.mail

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "app.mail")
class MailGatewayProperties {
  var domain: String = "feedless.org"
  var from: String = "no-reply@feedless.org"
}
