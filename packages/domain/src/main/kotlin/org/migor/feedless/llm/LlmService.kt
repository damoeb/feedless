package org.migor.feedless.llm

interface LlmService {
  suspend fun prompt(prompt: String): String
}
