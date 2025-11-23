package org.migor.feedless.document

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers
import org.migor.feedless.api.mapper.DocumentMapper
import org.migor.feedless.common.PropertyService
import org.migor.feedless.repository.RepositoryId
import org.mockito.Mockito
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.migor.feedless.generated.types.Record as RecordDto

class DocumentResolverTest {

    private lateinit var documentMapper: DocumentMapper
    private lateinit var propertyService: PropertyService

    @BeforeEach
    fun setUp() {
        documentMapper = Mappers.getMapper(DocumentMapper::class.java)
        propertyService = Mockito.mock(PropertyService::class.java)
    }

    @Test
    fun testDto() {
        val documentId = DocumentId()
        val repositoryId = RepositoryId()
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")
        val publishedAt = LocalDateTime.parse("2020-01-01T12:00:00")
        val updatedAt = LocalDateTime.parse("2020-01-02T12:00:00")

        val incoming = Document(
            id = documentId,
            url = "https://example.com/article",
            title = "Test Article",
            contentHash = "abc123hash",
            text = "This is the article text content.",
            html = "<p>This is the article text content.</p>",
            imageUrl = "https://example.com/image.jpg",
            publishedAt = publishedAt,
            updatedAt = updatedAt,
            repositoryId = repositoryId,
            status = ReleaseStatus.released,
            createdAt = createdAt,
            attachments = emptyList()
        )

        val result = documentMapper.toDto(incoming, propertyService)

        assertThat(result.id).isEqualTo(documentId.toString())
        assertThat(result.url).isEqualTo("https://example.com/article")
        assertThat(result.title).isEqualTo("Test Article")
        assertThat(result.text).isEqualTo("This is the article text content.")
        assertThat(result.html).isEqualTo("<p>This is the article text content.</p>")
        assertThat(result.imageUrl).isEqualTo("https://example.com/image.jpg")
        assertThat(result.publishedAt).isEqualTo(publishedAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
        assertThat(result.createdAt).isEqualTo(createdAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
        assertThat(result.updatedAt).isEqualTo(updatedAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
        assertThat(result.attachments).isEmpty()
    }


    @Test
    @Disabled
    fun `anonymous can retrieve records if repo is public`() {
        // todo test
    }

    @Test
    @Disabled
    fun `owner can retrieve records if repo is private`() {
        // todo test
    }
}
