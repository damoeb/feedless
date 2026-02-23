package org.migor.feedless.llm.ollama

data class OllamaResponse(
  val response: String,
  val done: Boolean? = null
)
