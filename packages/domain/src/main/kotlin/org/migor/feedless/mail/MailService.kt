package org.migor.feedless.mail

import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User

interface MailService {

  suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String)
}
