package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentGuardPort
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUseCasePort
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.http.mapper.HttpRecordMapper
import org.migor.feedless.repository.RepositoryId
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

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

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(document.id.uuid.toString()))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(document.id.uuid.toString()))
    }
  }

  private fun document(
    id: DocumentId = DocumentId(),
    repositoryId: RepositoryId = RepositoryId(UUID.randomUUID().toString()),
  ) = Document(
    id = id,
    url = "https://example.com/article",
    title = "Test record",
    contentHash = "hash",
    text = "body text",
    repositoryId = repositoryId,
    status = ReleaseStatus.released,
  )
}
