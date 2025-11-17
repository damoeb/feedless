package org.migor.feedless.api

import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordEntity
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.secrets.OneTimePasswordMapper


fun OneTimePasswordEntity.toDomain(): OneTimePassword {
  return OneTimePasswordMapper.Companion.INSTANCE.toDomain(this)
}
