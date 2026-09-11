package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.status.ServerStatus
import org.migor.feedless.status.ServerStatusPort
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

@WebMvcTest(controllers = [StatusHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.properties)
class StatusHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var serverStatus: ServerStatusPort

  @Test
  fun `getStatus answers version, build and the connected agent count`() = runTest {
    whenever(serverStatus.status()).thenReturn(
      ServerStatus(version = "0.3.0", commit = "abc123", buildDate = 1757000000000, connectedAgents = 2),
    )

    val mvcResult = mockMvc.get(StatusHttpController.PUBLIC_STATUS_PATH).andReturn()

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.version").value("0.3.0"))
      .andExpect(jsonPath("$.build.commit").value("abc123"))
      .andExpect(jsonPath("$.build.date").value(1757000000000))
      .andExpect(jsonPath("$.agents.connected").value(2))
  }

  @Test
  fun `getStatus exposes nothing beyond version, build and the agent count`() = runTest {
    whenever(serverStatus.status()).thenReturn(
      ServerStatus(version = "0.3.0", commit = "abc123", buildDate = 0, connectedAgents = 0),
    )

    val mvcResult = mockMvc.get(StatusHttpController.PUBLIC_STATUS_PATH).andReturn()

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.length()").value(3))
      .andExpect(jsonPath("$.build.length()").value(2))
      .andExpect(jsonPath("$.agents.length()").value(1))
      .andExpect(jsonPath("$.agents.connected").value(0))
  }
}
