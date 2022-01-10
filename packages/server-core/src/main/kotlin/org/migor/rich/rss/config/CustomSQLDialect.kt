package org.migor.rich.rss.config

import org.hibernate.dialect.PostgreSQL9Dialect
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.StandardBasicTypes

class CustomSQLDialect : PostgreSQL9Dialect() {
  init {
    registerFunction(
      "add_minutes",
      StandardSQLFunction(
        "add_minutes",
        StandardBasicTypes.TIMESTAMP
      )
    )
  }
}
