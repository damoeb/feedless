package org.migor.feedless.config

import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.spi.MetadataBuilderContributor
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.StandardBasicTypes

class SqlFunctionsMetadataBuilderContributor : MetadataBuilderContributor {
  override fun contribute(metadataBuilder: MetadataBuilder) {
    metadataBuilder.applySqlFunction(
      "add_minutes",
      StandardSQLFunction(
        "add_minutes",
        StandardBasicTypes.TIMESTAMP
      )
    )
  }
}
