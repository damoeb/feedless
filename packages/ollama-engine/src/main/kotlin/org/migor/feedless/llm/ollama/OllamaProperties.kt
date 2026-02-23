package org.migor.feedless.llm.ollama

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.llm.ollama")
data class OllamaProperties(
  val baseUrl: String,
  val model: String
)
