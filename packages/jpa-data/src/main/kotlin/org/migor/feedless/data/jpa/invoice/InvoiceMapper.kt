package org.migor.feedless.data.jpa.invoice

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.invoice.Invoice

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface InvoiceMapper {

  fun toDomain(entity: InvoiceEntity): Invoice
  fun toEntity(domain: Invoice): InvoiceEntity

  companion object {
    val INSTANCE: InvoiceMapper = Mappers.getMapper(InvoiceMapper::class.java)
  }
}


