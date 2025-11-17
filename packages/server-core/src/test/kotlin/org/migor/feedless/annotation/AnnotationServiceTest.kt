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
import org.migor.feedless.anyOrNull
import org.migor.feedless.argThat
import org.migor.feedless.data.jpa.annotation.AnnotationDAO
import org.migor.feedless.data.jpa.annotation.AnnotationEntity
import org.migor.feedless.data.jpa.annotation.TextAnnotationDAO
import org.migor.feedless.data.jpa.annotation.VoteDAO
import org.migor.feedless.data.jpa.annotation.VoteEntity
import org.migor.feedless.eq
import org.migor.feedless.generated.types.AnnotationWhereInput
import org.migor.feedless.generated.types.AnnotationWhereUniqueInput
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.OneOfAnnotationInput
import org.migor.feedless.generated.types.RecordUniqueWhereInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.session.RequestContext
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class AnnotationServiceTest {

  private lateinit var voteDAO: VoteDAO
  private lateinit var textAnnotationDAO: TextAnnotationDAO
  private lateinit var annotationDAO: AnnotationDAO
  private lateinit var annotationService: AnnotationService
  private lateinit var currentUser: UserEntity
  private val currentUserId = randomUserId()
  private val documentId = randomDocumentId()
  private val repositoryId = randomRepositoryId()

  @BeforeEach
  fun setUp() {
    voteDAO = mock(VoteDAO::class.java)
    `when`(voteDAO.save(any(VoteEntity::class.java))).thenAnswer { it.arguments[0] }
    textAnnotationDAO = mock(TextAnnotationDAO::class.java)
    annotationDAO = mock(AnnotationDAO::class.java)
    annotationService = AnnotationService(annotationDAO, voteDAO, textAnnotationDAO)

    currentUser = mock(UserEntity::class.java)
    `when`(currentUser.id).thenReturn(currentUserId.value)
  }

  @Test
  fun `given identical annotation exists, creating the same will be rejected`() {
    `when`(
      voteDAO.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(UUID::class.java),
        anyOrNull(UUID::class.java),
        eq(null),
      )
    ).thenReturn(true)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        annotationService.createAnnotation(
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
    mockAnnotationExists()
    annotationService.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          document = RecordUniqueWhereInput(documentId.value.toString())
        ),
        annotation = OneOfAnnotationInput(
          flag = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteDAO).save(argThat { it.flag && it.documentId == documentId.value })
  }

  @Test
  fun `upVote a document`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists()
    annotationService.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          document = RecordUniqueWhereInput(documentId.value.toString()),
        ),
        annotation = OneOfAnnotationInput(
          upVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteDAO).save(argThat { it.upVote && it.documentId == documentId.value })
  }

  @Test
  fun `downVote a document`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists()
    annotationService.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          document = RecordUniqueWhereInput(documentId.value.toString()),
        ),
        annotation = OneOfAnnotationInput(
          downVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteDAO).save(argThat { it.downVote && it.documentId == documentId.value })
  }

  @Test
  fun `flag a repository`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists()
    annotationService.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          repository = RepositoryUniqueWhereInput(repositoryId.value.toString()),
        ),
        annotation = OneOfAnnotationInput(
          flag = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteDAO).save(argThat { it.flag && it.repositoryId == repositoryId.value })
  }

  @Test
  fun `upVote a repository`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists()
    annotationService.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          repository = RepositoryUniqueWhereInput(repositoryId.value.toString()),
        ),
        annotation = OneOfAnnotationInput(
          upVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteDAO).save(argThat { it.upVote && it.repositoryId == repositoryId.value })
  }

  @Test
  fun `downVote a repository`() = runTest(context = RequestContext(userId = currentUserId)) {
    mockAnnotationExists()
    annotationService.createAnnotation(
      CreateAnnotationInput(
        where = AnnotationWhereInput(
          repository = RepositoryUniqueWhereInput(repositoryId.value.toString()),
        ),
        annotation = OneOfAnnotationInput(
          downVote = BoolUpdateOperationsInput(set = true)
        )
      ), currentUser
    )

    verify(voteDAO).save(argThat { it.downVote && it.repositoryId == repositoryId.value })
  }

  @Test
  fun `others cannot delete his annotation`() {
    val annotationId = UUID.randomUUID()
    val annotation = mock(AnnotationEntity::class.java)
    `when`(annotation.id).thenReturn(annotationId)
    `when`(annotation.ownerId).thenReturn(UUID.randomUUID())
    `when`(annotationDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(annotation))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        annotationService.deleteAnnotation(
          DeleteAnnotationInput(
            where = AnnotationWhereUniqueInput(annotationId.toString())
          ), currentUser
        )
      }
    }
  }

  private fun mockAnnotationExists() {
    `when`(
      voteDAO.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(Boolean::class.java),
        any(UUID::class.java),
        anyOrNull(UUID::class.java),
        anyOrNull(UUID::class.java),
      )
    ).thenReturn(false)
  }

}
