package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.Mother.randomOneTimePasswordEntity
import org.migor.feedless.Mother.randomUserEntity
import org.migor.feedless.any2
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.secrets.OneTimePasswordDAO
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailAuthenticationServiceTest {
  @Mock
  lateinit var jwtTokenIssuer: JwtTokenIssuer

  @Mock
  lateinit var cookieProvider: CookieProvider

  @Mock
  lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Mock
  lateinit var userService: UserService

  @Mock
  lateinit var mailService: MailService

  @Mock
  lateinit var featureService: FeatureService

  @Mock
  lateinit var userDAO: UserDAO

  @Mock
  lateinit var oneTimePasswordService: OneTimePasswordService

  @InjectMocks
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Test
  fun authenticateUsingMail() = runTest {
    // given
    val user = randomUserEntity()
    val otp = randomOneTimePasswordEntity(user)

    `when`(featureService.isDisabled(any2(), Mockito.isNull())).thenReturn(false)
    `when`(oneTimePasswordService.createOTP(any2())).thenReturn(otp)
    `when`(userDAO.findByEmail(any2())).thenReturn(user)

    val data = AuthViaMailInput(
      email = "someone@localhost",
      product = Vertical.feedless,
      osInfo = "Linux",
      allowCreate = false
    )

    // when
    val confirmCode = mailAuthenticationService.authenticateUsingMail(data)

    // then
    assertThat(confirmCode).isNotNull
    assertThat(confirmCode.otpId).isEqualTo(otp.id.toString())
    assertThat(confirmCode.length).isEqualTo(otp.password.length)
  }

  @Test
  fun confirmAuthCode() = runTest(context = RequestContext()) {
    // given
    val jwt = mock(Jwt::class.java)
    `when`(jwt.tokenValue).thenReturn(UUID.randomUUID().toString())

    val user = randomUserEntity()
    val otp = randomOneTimePasswordEntity(user)
    `when`(oneTimePasswordDAO.findById(otp.id)).thenReturn(Optional.of(otp))
    `when`(jwtTokenIssuer.createJwtForCapabilities(any2())).thenReturn(jwt)

    val data = ConfirmAuthCodeInput(
      code = otp.password,
      otpId = otp.id.toString(),
    )

    // when
    val authentication = mailAuthenticationService.confirmAuthCode(data, mock(HttpServletResponse::class.java))

    // then
    assertThat(authentication.token).isEqualTo(jwt.tokenValue)
  }
}
