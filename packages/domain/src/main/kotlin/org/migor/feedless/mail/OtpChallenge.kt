package org.migor.feedless.mail

import org.migor.feedless.otp.OneTimePasswordId

data class OtpChallenge(val length: Int, val otpId: OneTimePasswordId)
