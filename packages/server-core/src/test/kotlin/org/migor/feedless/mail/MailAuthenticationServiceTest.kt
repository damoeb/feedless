package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.Mother.randomOneTimePassword
import org.migor.feedless.Mother.randomUser
import org.migor.feedless.any2
import org.migor.feedless.capability.Capability
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.session.NoActingGroupException
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailAuthenticationServiceTest {
  @Mock
  lateinit var jwtTokenIssuer: JwtTokenIssuer

  @Mock
  lateinit var cookieProvider: CookieProvider

  @Mock
  lateinit var oneTimePasswordRepository: OneTimePasswordRepository

  @Mock
  lateinit var userUseCase: UserUseCase

  @Mock
  lateinit var mailService: MailService

  @Mock
  lateinit var featureService: FeatureService

  @Mock
  lateinit var userRepository: UserRepository

  @Mock
  lateinit var oneTimePasswordService: OneTimePasswordService

  @Mock
  lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

  @InjectMocks
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Test
  fun authenticateUsingMail() = runTest {
    // given
    val user = randomUser()
    val otp = randomOneTimePassword(user)

    `when`(featureService.isDisabled(any2(), Mockito.isNull())).thenReturn(false)
    `when`(oneTimePasswordService.createOTP(any2())).thenReturn(otp)
    `when`(userRepository.findByEmail(any2())).thenReturn(user)

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
    assertThat(confirmCode.otpId).isEqualTo(otp.id.uuid.toString())
    assertThat(confirmCode.length).isEqualTo(otp.password.length)
  }

  @Test
  fun confirmAuthCode() = runTest(context = RequestContext()) {
    // given
    val jwt = mock(Jwt::class.java)
    `when`(jwt.tokenValue).thenReturn(UUID.randomUUID().toString())

    val user = randomUser()
    val otp = randomOneTimePassword(user)
      .copy(validUntil = LocalDateTime.now().plusMinutes(10))
    `when`(oneTimePasswordRepository.findById(otp.id)).thenReturn(otp)
    `when`(jwtTokenIssuer.createJwtForCapabilities(any2())).thenReturn(jwt)
    val ownerGroupId = GroupId()
    `when`(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(
      listOf(UserGroupAssignment(userId = user.id, groupId = ownerGroupId, role = RoleInGroup.owner))
    )

    val data = ConfirmAuthCodeInput(
      code = otp.password,
      otpId = otp.id.uuid.toString(),
    )

    // when
    val authentication = mailAuthenticationService.confirmAuthCode(data, mock(HttpServletResponse::class.java))

    // then
    assertThat(authentication.token).isEqualTo(jwt.tokenValue)
    val capabilities = argumentCaptor<List<Capability<out Any>>>()
    verify(jwtTokenIssuer).createJwtForCapabilities(capabilities.capture())
    assertThat(capabilities.firstValue.filterIsInstance<UserCapability>().single().userId).isEqualTo(user.id)
    assertThat(capabilities.firstValue.filterIsInstance<GroupCapability>().single().group)
      .isEqualTo(GroupAndRole(ownerGroupId, RoleInGroup.owner))
  }

  @Test
  fun `confirmAuthCode issues no token for a user who owns no group`() {
    val user = randomUser()
    val otp = randomOneTimePassword(user)
      .copy(validUntil = LocalDateTime.now().plusMinutes(10))
    `when`(oneTimePasswordRepository.findById(otp.id)).thenReturn(otp)
    `when`(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(emptyList())
    val data = ConfirmAuthCodeInput(code = otp.password, otpId = otp.id.uuid.toString())

    assertThatExceptionOfType(NoActingGroupException::class.java).isThrownBy {
      runTest(context = RequestContext()) {
        mailAuthenticationService.confirmAuthCode(data, mock(HttpServletResponse::class.java))
      }
    }
    verify(jwtTokenIssuer, never()).createJwtForCapabilities(any2())
    // The code stays usable: the failure is the server's, not the user's.
    verify(oneTimePasswordRepository, never()).deleteById(any2())
  }
}
