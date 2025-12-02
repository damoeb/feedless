package org.migor.feedless.annotation

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomDocumentId
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any
import org.migor.feedless.argThat
import org.migor.feedless.document.DocumentId
import org.migor.feedless.eq
import org.migor.feedless.generated.types.AnnotationWhereInput
import org.migor.feedless.generated.types.AnnotationWhereUniqueInput
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.OneOfAnnotationInput
import org.migor.feedless.generated.types.RecordUniqueWhereInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class AnnotationServiceTest {

  private lateinit var voteRepository: VoteRepository
  private lateinit var textAnnotationRepository: TextAnnotationRepository
  private lateinit var annotationRepository: AnnotationRepository
  private lateinit var annotationUseCase: AnnotationUseCase
  private lateinit var currentUser: User
  private val currentUserId = randomUserId()
  private val documentId = randomDocumentId()
  private val repositoryId = randomRepositoryId()

  @BeforeEach
  fun setUp() = runTest {
    voteRepository = mock(VoteRepository::class.java)
    `when`(voteRepository.save(any(Vote::class.java))).thenAnswer { it.arguments[0] }
    textAnnotationRepository = mock(TextAnnotationRepository::class.java)
    annotationRepository = mock(AnnotationRepository::class.java)
    annotationUseCase = AnnotationUseCase(annotationRepository, voteRepository, textAnnotationRepository)

    currentUser = mock(User::class.java)
    `when`(currentUser.id).thenReturn(currentUserId)
  }

  @Test
  fun `given identical annotation exists, creating the same will be rejected`() {

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        mockAnnotationExists(true)

        annotationUseCase.createAnnotation(
          CreateAnnotationInput(
            where = AnnotationWhereInput(
              document = RecordUniqueWhereInput(UUID.randomUUID().toString())
            ),
            annotation = OneOfAnnotationInput(
              flag = BoolUpdateOperationsInput(set = true)
            )
          ), currentUser
        )
      }
    }
  }

  @Test
  fun `flag a document`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists(false)
    annotationUseCase.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          document = RecordUniqueWhereInput(documentId.uuid.toString())
        ),
        annotation = OneOfAnnotationInput(
          flag = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteRepository).save(argThat { it.flag && it.documentId == documentId })
  }

  @Test
  fun `upVote a document`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists(false)
    annotationUseCase.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          document = RecordUniqueWhereInput(documentId.uuid.toString()),
        ),
        annotation = OneOfAnnotationInput(
          upVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteRepository).save(argThat { it.upVote && it.documentId == documentId })
  }

  @Test
  fun `downVote a document`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists(false)
    annotationUseCase.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          document = RecordUniqueWhereInput(documentId.uuid.toString()),
        ),
        annotation = OneOfAnnotationInput(
          downVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteRepository).save(argThat { it.downVote && it.documentId == documentId })
  }

  @Test
  fun `flag a repository`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists(false)
    annotationUseCase.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          repository = RepositoryUniqueWhereInput(repositoryId.uuid.toString()),
        ),
        annotation = OneOfAnnotationInput(
          flag = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteRepository).save(argThat { it.flag && it.repositoryId == repositoryId })
  }

  @Test
  fun `upVote a repository`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists(false)
    annotationUseCase.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          repository = RepositoryUniqueWhereInput(repositoryId.uuid.toString()),
        ),
        annotation = OneOfAnnotationInput(
          upVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteRepository).save(argThat { it.upVote && it.repositoryId == repositoryId })
  }

  @Test
  fun `downVote a repository`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists(false)
    annotationUseCase.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          repository = RepositoryUniqueWhereInput(repositoryId.uuid.toString()),
        ),
        annotation = OneOfAnnotationInput(
          downVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteRepository).save(argThat { it.downVote && it.repositoryId == repositoryId })
  }

  @Test
  fun `others cannot delete his annotation`() {
    val annotationId = AnnotationId()
    val annotation = mock(TextAnnotation::class.java)
    `when`(annotation.id).thenReturn(annotationId)
    `when`(annotation.ownerId).thenReturn(UserId())

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        `when`(annotationRepository.findById(any(AnnotationId::class.java))).thenReturn(annotation)

        annotationUseCase.deleteAnnotation(
          DeleteAnnotationInput(
            where = AnnotationWhereUniqueInput(annotationId.uuid.toString())
          ), currentUser
        )
      }
    }
  }

  private suspend fun mockAnnotationExists(exists: Boolean = true) {
    `when`(
      voteRepository.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(UserId::class.java),
        any(DocumentId::class.java),
        eq(null),
      )
    ).thenReturn(exists)

    `when`(
      voteRepository.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(UserId::class.java),
        eq(null),
        any(RepositoryId::class.java),
      )
    ).thenReturn(exists)
  }

}
