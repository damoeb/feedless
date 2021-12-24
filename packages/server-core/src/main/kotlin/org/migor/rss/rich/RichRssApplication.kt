package org.migor.rss.rich

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class RichRssApplication

fun main(args: Array<String>) {
  runApplication<RichRssApplication>(*args)
}
