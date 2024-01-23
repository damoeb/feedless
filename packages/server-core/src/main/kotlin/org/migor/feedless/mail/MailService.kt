package org.migor.feedless.mail

import org.migor.feedless.api.auth.Email

interface MailService {
  fun send(mail: String, body: Email)
}
