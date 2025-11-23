package org.migor.feedless

import org.migor.feedless.connectedApp.ConnectedAppId
import org.migor.feedless.document.DocumentId
import org.migor.feedless.group.GroupId
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

object Mother {
    fun randomUserId(): UserId = UserId()
    fun randomGroupId(): GroupId = GroupId()
    fun randomDocumentId(): DocumentId = DocumentId()
    fun randomRepositoryId(): RepositoryId = RepositoryId()
    fun randomSourceId(): SourceId = SourceId()
    fun randomConnectedAppId(): ConnectedAppId = ConnectedAppId()

    fun randomUser(): User {
        return User(
            email = UUID.randomUUID().toString() + "@localhost",
            lastLogin = LocalDateTime.now(),
            hasAcceptedTerms = true,
        )
    }

    fun randomOneTimePassword(user: User): OneTimePassword {
        return OneTimePassword(
            password = "1234",
            validUntil = LocalDateTime.now(),
            userId = user.id,
        )
    }
}


fun <T> anyList(): List<T> = Mockito.anyList<T>()
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> any2(): T = ArgumentMatchers.any<T>()
fun <T> anyOrNull2(): T? = ArgumentMatchers.any<T?>()
fun <T> argThat(matcher: ArgumentMatcher<T>): T = Mockito.argThat<T>(matcher)
fun <T> anyOrNull(type: Class<T>): T? = Mockito.any<T?>(type)
fun <T> eq(type: T): T = Mockito.eq<T>(type) ?: type
