package org.migor.rss.rich.database.model

import com.vladmihalcea.hibernate.type.array.IntArrayType
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.MappedSuperclass

@MappedSuperclass
@TypeDefs(
  TypeDef(name = "string-array", typeClass = StringArrayType::class),
  TypeDef(name = "int-array", typeClass = IntArrayType::class),
  TypeDef(name = "json", typeClass = JsonStringType::class),
  TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
)
open class JsonSupport
