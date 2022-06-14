package org.migor.rich.rss.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@EnableCaching
@Profile("!nocache")
class CacheConfig
