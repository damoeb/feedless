package org.migor.feedless.karma

import org.junit.jupiter.api.BeforeEach
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test

class KarmaCommentsTest {

  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var documentRepository: DocumentRepository
  private lateinit var userRepository: UserRepository
  private lateinit var karmaChangeRepository: KarmaChangeRepository
  private lateinit var karmaComments: KarmaComments
  private lateinit var karmaHandlerFactory: KarmaHandlerFactory
  private lateinit var documentHandlerFactory: DocumentHandlerFactory

  @BeforeEach
  fun setUp() {

    userRepository = mock(UserRepository::class.java)
    karmaChangeRepository = mock(KarmaChangeRepository::class.java)
    karmaHandlerFactory = KarmaHandlerFactory(userRepository, karmaChangeRepository)

    documentRepository = mock(DocumentRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)

    documentHandlerFactory = DocumentHandlerFactory(documentRepository, repositoryRepository)
    karmaComments = KarmaComments(karmaHandlerFactory, documentHandlerFactory)
  }

  @Test
  fun voteUp() {
    val actor = UserId()
    val documentId = DocumentId()
    val repositoryId = RepositoryId()

    val document = mock(Document::class.java)
    `when`(document.repositoryId).thenReturn(repositoryId)

    `when`(documentRepository.findById(documentId)).thenReturn(document)


    val repository = mock(Repository::class.java)
    `when`(repository.ownerId).thenReturn(actor)

    `when`(repositoryRepository.findById(repositoryId)).thenReturn(repository)

    val user = mock(User::class.java)
    `when`(user.karma).thenReturn(0)
    `when`(userRepository.findById(actor)).thenReturn(user)
    `when`(userRepository.save(user)).thenReturn(user)

    karmaComments.onUpvote(actor, documentId)
  }

}
