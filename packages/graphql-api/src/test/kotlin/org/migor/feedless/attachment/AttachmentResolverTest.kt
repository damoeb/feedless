package org.migor.feedless.attachment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.Attachment as AttachmentDto
import java.time.LocalDateTime

class AttachmentResolverTest {

    @Test
    fun testDto() {
        val attachmentId = AttachmentId()
        val documentId = DocumentId()
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")

        val incoming = Attachment(
            id = attachmentId,
            hasData = true,
            data = "test data".toByteArray(),
            remoteDataUrl = "https://example.com/file.pdf",
            mimeType = "application/pdf",
            originalUrl = "https://source.com/original.pdf",
            name = "document.pdf",
            size = 1024L,
            duration = 60L,
            documentId = documentId,
            createdAt = createdAt
        )

        val expected = AttachmentDto(
            id = attachmentId.uuid.toString(),
            type = "application/pdf",
            url = "https://example.com/file.pdf",
            size = 1024L,
            duration = 60L
        )

        assertThat(incoming.toDto()).isEqualTo(expected)
    }


}
