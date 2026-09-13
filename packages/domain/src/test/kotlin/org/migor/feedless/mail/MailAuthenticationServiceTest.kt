package org.migor.feedless.mail

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.Mother.randomOneTimePassword
import org.migor.feedless.Mother.randomUser
import org.migor.feedless.any2
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.capability.Capability
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.NoActingGroupException
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.quality.Strictness
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailAuthenticationServiceTest {
  @Mock
  lateinit var tokenIssuer: TokenIssuer

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

    // when
    val confirmCode = mailAuthenticationService.authenticateUsingMail(
      email = "someone@localhost",
      allowCreate = false,
      osInfo = "Linux",
    )

    // then
    assertThat(confirmCode).isNotNull
    assertThat(confirmCode.otpId.uuid.toString()).isEqualTo(otp.id.uuid.toString())
    assertThat(confirmCode.length).isEqualTo(otp.password.length)
  }

  @Test
  fun confirmAuthCode() = runTest(context = RequestContext()) {
    // given
    val token = AuthToken(UUID.randomUUID().toString())

    val user = randomUser()
    val otp = randomOneTimePassword(user)
      .copy(validUntil = LocalDateTime.now().plusMinutes(10))
    `when`(oneTimePasswordRepository.findById(otp.id)).thenReturn(otp)
    `when`(tokenIssuer.issueTokenForCapabilities(any2())).thenReturn(token)
    val ownerGroupId = GroupId()
    `when`(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(
      listOf(UserGroupAssignment(userId = user.id, groupId = ownerGroupId, role = RoleInGroup.owner))
    )

    // when
    val authentication = mailAuthenticationService.confirmAuthCode(otp.id, otp.password)

    // then
    assertThat(authentication.token).isEqualTo(token.token)
    val capabilities = argumentCaptor<List<Capability<out Any>>>()
    verify(tokenIssuer).issueTokenForCapabilities(capabilities.capture())
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

    assertThatExceptionOfType(NoActingGroupException::class.java).isThrownBy {
      runTest(context = RequestContext()) {
        mailAuthenticationService.confirmAuthCode(otp.id, otp.password)
      }
    }
    verify(tokenIssuer, never()).issueTokenForCapabilities(any2())
    // The code stays usable: the failure is the server's, not the user's.
    verify(oneTimePasswordRepository, never()).deleteById(any2())
  }
}
