package org.migor.feedless.http

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackages = ["org.migor.feedless.http"])
class HttpApiAutoConfiguration
