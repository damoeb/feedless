package org.migor.feedless.mail

import org.migor.feedless.data.jpa.enums.Product
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.models.UserEntity

interface MailService {
  fun sendWelcomeMail(corrId: String, user: UserEntity)
  fun sendAuthCode(corrId: String, user: UserEntity, otp: OneTimePasswordEntity, description: String)
  fun getNoReplyAddress(product: Product): String
}
