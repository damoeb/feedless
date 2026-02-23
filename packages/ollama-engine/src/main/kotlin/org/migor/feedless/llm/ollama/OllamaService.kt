package org.migor.feedless.llm.ollama

import org.migor.feedless.llm.LlmService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@ConditionalOnBean(OllamaConfig::class)
class OllamaService(val properties: OllamaProperties) : LlmService {

  private val log = LoggerFactory.getLogger(OllamaService::class.java)

  private val client = RestClient.builder()
    .baseUrl(properties.baseUrl)
    .build()

  /*
  docker run -d \
  --name ollama \
  -p 11434:11434 \
  -v ollama:/root/.ollama \
  ollama/ollama
   */
  override suspend fun prompt(prompt: String): String {
    val request = OllamaRequest(
      model = properties.model,
      prompt = prompt,
      stream = false
    )

    val response = client.post()
      .uri("/api/generate")
      .body(request)
      .retrieve()
      .body(OllamaResponse::class.java)

    return response?.response ?: "No response from model"
  }

}
