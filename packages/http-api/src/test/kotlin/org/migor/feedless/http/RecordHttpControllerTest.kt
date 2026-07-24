package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentCreate
import org.migor.feedless.document.DocumentGuardPort
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUpdate
import org.migor.feedless.document.DocumentUseCasePort
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.document.StringFilter
import org.migor.feedless.http.mapper.HttpRecordMapper
import org.migor.feedless.repository.RepositoryId
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [RecordHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpRecordMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.document)
class RecordHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var documentUseCase: DocumentUseCasePort

  @MockitoBean
  private lateinit var documentGuard: DocumentGuardPort

  @Test
  fun `getRecord returns record`() = runTest {
    val document = document()
    whenever(documentGuard.requireRead(any())).thenReturn(document)

    val mvcResult = mockMvc.get("/api/v1/records/${document.id.uuid}").andReturn()
    assertRecordResponse(mvcResult, document.id.uuid.toString(), document.title!!)
  }

  @Test
  fun `listRecords sets hasMore when a further item exists`() = runTest {
    val repositoryId = RepositoryId()
    // pageSize + 1 available: there really is a next page
    val records = List(3) { document(repositoryId = repositoryId) }
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

    val mvcResult = mockMvc.get("/api/v1/records") {
      param("repositoryId", repositoryId.uuid.toString())
      param("page", "0")
      param("pageSize", "2")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.hasMore").value(true))
        .andExpect(jsonPath("$.items.length()").value(2))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("\"hasMore\":true"))
    }
  }

  @Test
  fun `listRecords sets hasMore false when page is not full`() = runTest {
    val repositoryId = RepositoryId()
    val records = listOf(document(repositoryId = repositoryId))
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

    val mvcResult = mockMvc.get("/api/v1/records") {
      param("repositoryId", repositoryId.uuid.toString())
      param("page", "0")
      param("pageSize", "2")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.hasMore").value(false))
        .andExpect(jsonPath("$.items.length()").value(1))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("\"hasMore\":false"))
    }
  }

  @Test
  fun `listRecords reports hasMore false when the last page is exactly full`() = runTest {
    val repositoryId = RepositoryId()
    // Exactly pageSize available. The old `items.size == pageSize` rule claimed a next
    // page here and made every client fetch an empty one.
    val records = List(2) { document(repositoryId = repositoryId) }
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

    val mvcResult = mockMvc.get("/api/v1/records") {
      param("repositoryId", repositoryId.uuid.toString())
      param("page", "0")
      param("pageSize", "2")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.hasMore").value(false))
        .andExpect(jsonPath("$.items.length()").value(2))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("\"hasMore\":false"))
    }
  }

  @Test
  fun `createRecord returns 201`() = runTest {
    val repositoryId = RepositoryId()
    val created = document(repositoryId = repositoryId)
    whenever(documentUseCase.createDocument(any())).thenReturn(created)

    val mvcResult = mockMvc.post("/api/v1/records") {
      contentType = MediaType.APPLICATION_JSON
      content = """
        {
          "title": "New record",
          "url": "https://example.com/new",
          "publishedAt": 1700000000000,
          "repositoryId": "${repositoryId.uuid}"
        }
      """.trimIndent()
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").value(created.id.uuid.toString()))
        .andExpect(jsonPath("$.title").value(created.title))
    } else {
      assert(mvcResult.response.status == 201)
      assert(mvcResult.response.contentAsString.contains(created.id.uuid.toString()))
    }
    verify(documentUseCase).createDocument(any<DocumentCreate>())
  }

  @Test
  fun `updateRecord returns updated record`() = runTest {
    val recordId = DocumentId()
    val updated = document(id = recordId, title = "Updated title")
    whenever(documentUseCase.updateDocument(any(), eq(recordId))).thenReturn(updated)

    val mvcResult = mockMvc.patch("/api/v1/records/${recordId.uuid}") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"title":"Updated title"}"""
    }.andReturn()

    assertRecordResponse(mvcResult, recordId.uuid.toString(), "Updated title")
    verify(documentUseCase).updateDocument(any<DocumentUpdate>(), eq(recordId))
  }

  @Test
  fun `deleteRecords returns 204`() = runTest {
    val repositoryId = RepositoryId()
    val recordId = DocumentId()

    val mvcResult = mockMvc.delete("/api/v1/records") {
      param("repositoryId", repositoryId.uuid.toString())
      contentType = MediaType.APPLICATION_JSON
      content = """{"ids":["${recordId.uuid}"]}"""
    }.andReturn()

    dispatchIfAsync(mvcResult, status().isNoContent)
    verify(documentUseCase).deleteDocuments(
      eq(repositoryId),
      eq(StringFilter(`in` = listOf(recordId.uuid.toString()))),
    )
  }

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

  private fun assertRecordResponse(
    mvcResult: org.springframework.test.web.servlet.MvcResult,
    expectedId: String,
    expectedTitle: String,
  ) {
    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(expectedId))
        .andExpect(jsonPath("$.title").value(expectedTitle))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(expectedId))
      assert(mvcResult.response.contentAsString.contains(expectedTitle))
    }
  }

  private fun dispatchIfAsync(
    mvcResult: org.springframework.test.web.servlet.MvcResult,
    expectedStatus: org.springframework.test.web.servlet.ResultMatcher,
  ) {
    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult)).andExpect(expectedStatus)
    } else {
      expectedStatus.match(mvcResult)
    }
  }
}
