package org.migor.feedless

import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordEntity
import org.migor.feedless.document.DocumentId
import org.migor.feedless.group.GroupId
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.ConnectedAppId
import org.migor.feedless.user.UserId
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

object Mother {
  fun randomUserId(): UserId = UserId(UUID.randomUUID())
  fun randomGroupId(): GroupId = GroupId(UUID.randomUUID())
  fun randomDocumentId(): DocumentId = DocumentId(UUID.randomUUID())
  fun randomRepositoryId(): RepositoryId = RepositoryId(UUID.randomUUID())
  fun randomSourceId(): SourceId = SourceId(UUID.randomUUID())
  fun randomConnectedAppId(): ConnectedAppId = ConnectedAppId(UUID.randomUUID())

  fun randomUserEntity(): UserEntity {
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(UUID.randomUUID())
    `when`(user.email).thenReturn(UUID.randomUUID().toString() + "@localhost")
    `when`(user.hasValidatedEmail).thenReturn(true)
    `when`(user.admin).thenReturn(false)
    `when`(user.anonymous).thenReturn(false)
    `when`(user.karma).thenReturn(0)
    `when`(user.shadowBanned).thenReturn(false)
    `when`(user.banned).thenReturn(false)
    `when`(user.hasAcceptedTerms).thenReturn(true)
    `when`(user.locked).thenReturn(true)
    `when`(user.lastLogin).thenReturn(LocalDateTime.now())

    return user
  }

  fun randomOneTimePasswordEntity(user: UserEntity): OneTimePasswordEntity {
    val otp = OneTimePasswordEntity()
    otp.password = UUID.randomUUID().toString()
    otp.validUntil = LocalDateTime.now().plusMinutes(3)
    otp.userId = user.id
    otp.user = user
    return otp
  }
}


fun <T> anyList(): List<T> = Mockito.anyList<T>()
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> any2(): T = ArgumentMatchers.any<T>()
fun <T> anyOrNull2(): T? = ArgumentMatchers.any<T?>()
fun <T> argThat(matcher: ArgumentMatcher<T>): T = Mockito.argThat<T>(matcher)
fun <T> anyOrNull(type: Class<T>): T? = Mockito.any<T?>(type)
fun <T> eq(type: T): T = Mockito.eq<T>(type) ?: type
