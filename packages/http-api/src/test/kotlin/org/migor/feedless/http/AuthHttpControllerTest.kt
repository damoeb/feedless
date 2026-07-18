package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.http.mapper.HttpAuthMapper
import org.migor.feedless.session.HttpSetCookie
import org.migor.feedless.session.SessionTokenPort
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AuthHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpAuthMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.session)
class AuthHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var sessionTokenPort: SessionTokenPort

  @Test
  fun `login returns token`() = runTest {
    whenever(sessionTokenPort.authenticateUser(any(), any())).thenReturn(AuthToken("jwt-token"))
    whenever(sessionTokenPort.toCookie(any())).thenReturn(
      HttpSetCookie(name = "TOKEN", value = "jwt-token", maxAge = 3600),
    )

    val mvcResult = mockMvc.post("/api/v1/auth/login") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"email":"user@example.com","secretKey":"secret"}"""
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.corrId").exists())
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("jwt-token"))
    }
  }
}
