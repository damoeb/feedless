package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.PageableRequest
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentCreate
import org.migor.feedless.document.DocumentGuardPort
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUpdate
import org.migor.feedless.document.DocumentUseCasePort
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.document.StringFilter
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.http.mapper.HttpRecordMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.source.SourceRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(controllers = [RecordHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpRecordMapper::class, HttpApiExceptionHandler::class, RepositoryAccessGuard::class, RequestContextBridge::class)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppProfiles.document,
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
)
class RecordHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var documentUseCase: DocumentUseCasePort

  @MockitoBean
  private lateinit var documentGuard: DocumentGuardPort

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCasePort

  @MockitoBean
  private lateinit var groupUseCase: GroupUseCasePort

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  private val access by lazy { RepositoryAccessFixture(repositoryUseCase, groupUseCase) }

  @Test
  fun `getRecord returns record`() = runTest {
    val repo = access.givenRepository()
    val document = givenRecord(repo.id)

    val result = mockMvc.getAs(access.owner, recordUrl(repo, document.id))

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains(document.id.uuid.toString())) { result.response.contentAsString }
    assert(result.response.contentAsString.contains(document.title!!)) { result.response.contentAsString }
  }

  @Test
  fun `getRecord answers a group member and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertStatus(mockMvc.getAs(access.member, recordUrl(private, givenRecord(private.id).id)), 200)
    assertStatus(mockMvc.getAs(access.stranger, recordUrl(public, givenRecord(public.id).id)), 200)
  }

  @Test
  fun `getRecord answers a stranger on a private repository like a missing one`() = runTest {
    val private = access.givenRepository()
    val document = givenRecord(private.id)

    assertNotFound(mockMvc.getAs(access.stranger, recordUrl(private, document.id)), "repository ${private.id.uuid} not found")
    verify(documentGuard, never()).requireRead(any())
  }

  @Test
  fun `getRecord returns 404 when record belongs to another repository`() = runTest {
    val repo = access.givenRepository()
    val foreign = givenRecord(RepositoryId())

    assertNotFound(mockMvc.getAs(access.owner, recordUrl(repo, foreign.id)), "record ${foreign.id.uuid} not found")
  }

  @Test
  fun `a missing record answers get, update and delete exactly like a record of another repository`() = runTest {
    val repo = access.givenRepository()
    val missing = givenMissingRecord()
    val foreign = givenRecord(RepositoryId()).id

    for (recordId in listOf(missing, foreign)) {
      val url = recordUrl(repo, recordId)
      val responses = listOf(
        mockMvc.getAs(access.owner, url),
        mockMvc.patchAs(access.owner, url, UPDATE),
        mockMvc.deleteAs(access.owner, url),
      )
      for (result in responses) {
        assertNotFound(result, "record ${recordId.uuid} not found")
        // DocumentGuard's own wording would tell a missing record from a foreign one.
        assert(!result.response.contentAsString.contains("Document")) { result.response.contentAsString }
      }
    }
    verify(documentUseCase, never()).updateDocument(any(), any())
    verify(documentUseCase, never()).deleteDocuments(any(), any())
  }

  /** A record id DocumentGuard does not know, stubbed with the message production really sends. */
  private suspend fun givenMissingRecord(): DocumentId {
    val id = DocumentId()
    // DocumentGuard.requireRead/requireWrite: NotFoundException("Document $id not found")
    val productionMessage = NotFoundException("Document $id not found")
    whenever(documentGuard.requireRead(eq(id))).thenThrow(productionMessage)
    whenever(documentGuard.requireWrite(eq(id))).thenThrow(productionMessage)
    return id
  }

  @Test
  fun `listRecords sets hasMore when a further item exists`() = runTest {
    val repo = access.givenRepository()
    // pageSize + 1 available: there really is a next page
    givenRecordPage(repo.id, List(3) { document(repositoryId = repo.id) })

    val result = mockMvc.getAs(access.owner, "${recordsUrl(repo)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":true")) { result.response.contentAsString }
  }

  @Test
  fun `listRecords sets hasMore false when page is not full`() = runTest {
    val repo = access.givenRepository()
    givenRecordPage(repo.id, listOf(document(repositoryId = repo.id)))

    val result = mockMvc.getAs(access.owner, "${recordsUrl(repo)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":false")) { result.response.contentAsString }
  }

  @Test
  fun `listRecords reports hasMore false when the last page is exactly full`() = runTest {
    val repo = access.givenRepository()
    // Exactly pageSize available. The old `items.size == pageSize` rule claimed a next
    // page here and made every client fetch an empty one.
    givenRecordPage(repo.id, List(2) { document(repositoryId = repo.id) })

    val result = mockMvc.getAs(access.owner, "${recordsUrl(repo)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":false")) { result.response.contentAsString }
  }

  @Test
  fun `listRecords answers a group member and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)
    givenRecordPage(private.id, emptyList())
    givenRecordPage(public.id, emptyList())

    assertStatus(mockMvc.getAs(access.member, "${recordsUrl(private)}?page=0&pageSize=2"), 200)
    assertStatus(mockMvc.getAs(access.stranger, "${recordsUrl(public)}?page=0&pageSize=2"), 200)
  }

  @Test
  fun `listRecords answers a stranger on a private repository like a missing one`() = runTest {
    val private = access.givenRepository()

    assertNotFound(mockMvc.getAs(access.stranger, recordsUrl(private)), "repository ${private.id.uuid} not found")
    verify(documentUseCase, never()).findAllByRepositoryId(any(), anyOrNull(), anyOrNull(), any(), any(), any())
  }

  @Test
  fun `createRecord lets the owner and a group member through the guard to the use case`() = runTest {
    val repo = access.givenRepository()
    val created = document(repositoryId = repo.id)
    whenever(documentUseCase.createDocument(any())).thenReturn(created)

    val result = mockMvc.postAs(access.owner, recordsUrl(repo), CREATE)
    assertStatus(result, 201)
    assert(result.response.contentAsString.contains(created.id.uuid.toString())) { result.response.contentAsString }
    assertStatus(mockMvc.postAs(access.member, recordsUrl(repo), CREATE), 201)
    verify(documentUseCase, org.mockito.kotlin.times(2)).createDocument(any<DocumentCreate>())
  }

  @Test
  fun `createRecord answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(mockMvc.postAs(access.stranger, recordsUrl(private), CREATE), "repository ${private.id.uuid} not found")
    assertNotFound(mockMvc.postAs(access.stranger, recordsUrl(public), CREATE), "repository ${public.id.uuid} not found")
    verify(documentUseCase, never()).createDocument(any())
  }

  @Test
  fun `updateRecord lets the owner and a group member through the guard to the use case`() = runTest {
    val repo = access.givenRepository()
    val record = givenRecord(repo.id)
    val updated = document(id = record.id, repositoryId = repo.id, title = "Updated title")
    whenever(documentUseCase.updateDocument(any(), eq(record.id))).thenReturn(updated)

    val result = mockMvc.patchAs(access.owner, recordUrl(repo, record.id), UPDATE)
    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("Updated title")) { result.response.contentAsString }
    assertStatus(mockMvc.patchAs(access.member, recordUrl(repo, record.id), UPDATE), 200)
    verify(documentUseCase, org.mockito.kotlin.times(2)).updateDocument(any<DocumentUpdate>(), eq(record.id))
  }

  @Test
  fun `updateRecord answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(
      mockMvc.patchAs(access.stranger, recordUrl(private, givenRecord(private.id).id), UPDATE),
      "repository ${private.id.uuid} not found",
    )
    assertNotFound(
      mockMvc.patchAs(access.stranger, recordUrl(public, givenRecord(public.id).id), UPDATE),
      "repository ${public.id.uuid} not found",
    )
    verify(documentUseCase, never()).updateDocument(any(), any())
  }

  @Test
  fun `updateRecord returns 404 when record belongs to another repository`() = runTest {
    val repo = access.givenRepository()
    val foreign = givenRecord(RepositoryId())

    assertNotFound(mockMvc.patchAs(access.owner, recordUrl(repo, foreign.id), UPDATE), "record ${foreign.id.uuid} not found")
    verify(documentUseCase, never()).updateDocument(any(), any())
  }

  @Test
  fun `deleteRecord lets the owner and a group member through the guard to the use case`() = runTest {
    val repo = access.givenRepository()
    val record = givenRecord(repo.id)

    assertStatus(mockMvc.deleteAs(access.owner, recordUrl(repo, record.id)), 204)
    assertStatus(mockMvc.deleteAs(access.member, recordUrl(repo, record.id)), 204)
    verify(documentUseCase, org.mockito.kotlin.times(2)).deleteDocuments(
      eq(repo.id),
      eq(StringFilter(`in` = listOf(record.id.uuid.toString()))),
    )
  }

  @Test
  fun `deleteRecord answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(
      mockMvc.deleteAs(access.stranger, recordUrl(private, givenRecord(private.id).id)),
      "repository ${private.id.uuid} not found",
    )
    assertNotFound(
      mockMvc.deleteAs(access.stranger, recordUrl(public, givenRecord(public.id).id)),
      "repository ${public.id.uuid} not found",
    )
    verify(documentUseCase, never()).deleteDocuments(any(), any())
  }

  @Test
  fun `deleteRecord returns 404 when record belongs to another repository`() = runTest {
    val repo = access.givenRepository()
    val foreign = givenRecord(RepositoryId())

    assertNotFound(mockMvc.deleteAs(access.owner, recordUrl(repo, foreign.id)), "record ${foreign.id.uuid} not found")
    verify(documentUseCase, never()).deleteDocuments(any(), any())
  }

  /** A record the document guard resolves for both reads and writes. */
  private suspend fun givenRecord(repositoryId: RepositoryId): Document {
    val document = document(repositoryId = repositoryId)
    whenever(documentGuard.requireRead(eq(document.id))).thenReturn(document)
    whenever(documentGuard.requireWrite(eq(document.id))).thenReturn(document)
    return document
  }

  private suspend fun givenRecordPage(repositoryId: RepositoryId, records: List<Document>) {
    whenever(
      documentUseCase.findAllByRepositoryId(
        eq(repositoryId),
        anyOrNull(),
        anyOrNull(),
        any(),
        any(),
        eq(PageableRequest(pageNumber = 0, pageSize = 3)),
      ),
    ).thenReturn(records)
  }

  private fun recordsUrl(repo: Repository) = "/api/v1/repositories/${repo.id.uuid}/records"

  private fun recordUrl(repo: Repository, recordId: DocumentId) = "${recordsUrl(repo)}/${recordId.uuid}"

  private fun document(
    id: DocumentId = DocumentId(),
    repositoryId: RepositoryId = RepositoryId(),
    title: String = "Test record",
  ) = Document(
    id = id,
    url = "https://example.com/article",
    title = title,
    contentHash = "hash",
    text = "body text",
    repositoryId = repositoryId,
    status = ReleaseStatus.released,
  )

  private companion object {
    const val UPDATE = """{"title":"Updated title"}"""
    const val CREATE = """{"title":"New record","url":"https://example.com/new","publishedAt":1700000000000}"""
  }
}
