package org.migor.rich.rss.config

import org.migor.rich.rss.AppProfiles
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@EnableCaching
@Profile("!${AppProfiles.nocache}")
class CacheConfig
