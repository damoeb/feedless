package org.migor.feedless.annotation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.BoolAnnotation
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime
import org.migor.feedless.generated.types.Annotation as AnnotationDto
import org.migor.feedless.generated.types.TextAnnotation as TextAnnotationDto

class AnnotationResolverTest {

    @Test
    fun testToTextAnnotationDto() {
        val annotationId = AnnotationId()
        val repositoryId = RepositoryId()
        val documentId = DocumentId()
        val ownerId = UserId()
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")

        val incoming = TextAnnotation(
            fromChar = 10,
            toChar = 50,
            id = annotationId,
            repositoryId = repositoryId,
            documentId = documentId,
            ownerId = ownerId,
            createdAt = createdAt
        )

        val expected = AnnotationDto(
            id = annotationId.uuid.toString(),
            text = TextAnnotationDto(
                fromChar = 10,
                toChar = 50,
            ),
            flag = null,
            upVote = null,
            downVote = null
        )

        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testVoteToBoolAnnotationDto() {
        val annotationId = AnnotationId()
        val repositoryId = RepositoryId()
        val documentId = DocumentId()
        val ownerId = UserId()
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")

        val incoming = Vote(
            upVote = true,
            downVote = false,
            flag = false,
            id = annotationId,
            repositoryId = repositoryId,
            documentId = documentId,
            ownerId = ownerId,
            createdAt = createdAt
        )

        val expected = AnnotationDto(
            id = annotationId.uuid.toString(),
            text = null,
            flag = BoolAnnotation(value = false),
            upVote = BoolAnnotation(value = true),
            downVote = BoolAnnotation(value = false)
        )

        assertThat(incoming.toDto()).isEqualTo(expected)
    }

}
