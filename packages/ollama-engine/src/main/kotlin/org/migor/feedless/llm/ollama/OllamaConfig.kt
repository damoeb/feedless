package org.migor.feedless.llm.ollama

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OllamaProperties::class)
class OllamaConfig
