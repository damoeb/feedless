package org.migor.rich.rss

import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
class RichRssApplication

fun main(args: Array<String>) {
  runApplication<RichRssApplication>(*args)
}
