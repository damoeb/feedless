package org.migor.feedless.session

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.document.Document
import org.migor.feedless.eq
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PermissionServiceTest {

    private lateinit var permissionService: PermissionService
    private lateinit var userDAO: UserRepository
    private lateinit var repositoryDAO: RepositoryRepository
    private val currentUserId = randomUserId()
    private val repositoryId = randomRepositoryId()

    @BeforeEach
    fun setUp() = runTest {
        userDAO = mock(UserRepository::class.java)
        repositoryDAO = mock(RepositoryRepository::class.java)
        permissionService = PermissionService(userDAO, repositoryDAO)

        mockUser(currentUserId)
    }

    @Test
    fun `canWrite document for owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
        val document = mock(Document::class.java)
        `when`(document.repositoryId).thenReturn(repositoryId)
        mockRepository(repositoryId, ownerId = currentUserId)
        permissionService.canWrite(document)
    }

    @Test
    fun `canWrite document for non-owner fails`() {
        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = currentUserId)) {
                val document = mock(Document::class.java)
                `when`(document.repositoryId).thenReturn(repositoryId)
                mockRepository(repositoryId, ownerId = randomUserId())
                permissionService.canWrite(document)
            }
        }
    }

    @Test
    fun `canWrite repository for owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
        val repository = mockRepository(repositoryId, ownerId = currentUserId)
        permissionService.canWrite(repository)
    }

    @Test
    fun `canWrite repository for non-owner fails`() {
        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = currentUserId)) {
                val repository = mockRepository(repositoryId, ownerId = randomUserId())
                permissionService.canWrite(repository)
            }
        }
    }

    private suspend fun mockRepository(repositoryId: RepositoryId, ownerId: UserId): Repository {
        val repository = mock(Repository::class.java)
        `when`(repository.id).thenReturn(repositoryId)
        `when`(repository.ownerId).thenReturn(ownerId)
        `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(repository)
        return repository
    }

    private suspend fun mockUser(userId: UserId): User {
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(userId)
        `when`(userDAO.findById(eq(userId))).thenReturn(user)
        return user
    }
}
