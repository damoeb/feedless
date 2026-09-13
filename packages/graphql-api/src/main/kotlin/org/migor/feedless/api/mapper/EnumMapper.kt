package org.migor.feedless.api.mapper

import org.mapstruct.Mapper
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.pipeline.plugins.CompareBy
import org.migor.feedless.pipeline.plugins.RecordField
import org.migor.feedless.pipeline.plugins.StringFilterOperator
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.scrape.ExtendContext
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.userSecret.UserSecretType
import org.migor.feedless.generated.types.CompareBy as CompareByDto
import org.migor.feedless.generated.types.ExtendContentOptions as ExtendContentOptionsDto
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto
import org.migor.feedless.generated.types.RecordDateField as RecordDateFieldDto
import org.migor.feedless.generated.types.RecordField as RecordFieldDto
import org.migor.feedless.generated.types.ScrapeEmit as ScrapeEmitDto
import org.migor.feedless.generated.types.StringFilterOperator as StringFilterOperatorDto
import org.migor.feedless.generated.types.UserSecretType as UserSecretTypeDto
import org.migor.feedless.generated.types.Vertical as VerticalDto
import org.migor.feedless.generated.types.Visibility as VisibilityDto

/**
 * MapStruct mapper for enum conversions between domain and DTO
 */
@Mapper(config = MapStructConfig::class)
interface EnumMapper {

  // EntityVisibility <-> VisibilityDto
  fun toDto(visibility: EntityVisibility): VisibilityDto = when (visibility) {
    EntityVisibility.isPrivate -> VisibilityDto.isPrivate
    EntityVisibility.isPublic -> VisibilityDto.isPublic
  }

  fun fromDto(dto: VisibilityDto): EntityVisibility = when (dto) {
    VisibilityDto.isPrivate -> EntityVisibility.isPrivate
    VisibilityDto.isPublic -> EntityVisibility.isPublic
  }

  // Vertical <-> VerticalDto
  fun toDto(vertical: Vertical): VerticalDto = when (vertical) {
    Vertical.all -> VerticalDto.all
    Vertical.visualDiff -> VerticalDto.visualDiff
    Vertical.rssProxy -> VerticalDto.rssProxy
    Vertical.reader -> VerticalDto.reader
    Vertical.feedless -> VerticalDto.feedless
    Vertical.feedDump -> VerticalDto.feedDump
    Vertical.untoldNotes -> VerticalDto.untoldNotes
    Vertical.upcoming -> VerticalDto.upcoming
    else -> throw IllegalArgumentException("$vertical is not a valid vertical")
  }

  fun fromDto(dto: VerticalDto): Vertical = when (dto) {
    VerticalDto.all -> Vertical.all
    VerticalDto.visualDiff -> Vertical.visualDiff
    VerticalDto.rssProxy -> Vertical.rssProxy
    VerticalDto.reader -> Vertical.reader
    VerticalDto.feedless -> Vertical.feedless
    VerticalDto.feedDump -> Vertical.feedDump
    VerticalDto.untoldNotes -> Vertical.untoldNotes
    VerticalDto.upcoming -> Vertical.upcoming
    else -> throw IllegalArgumentException("$dto is not a valid vertical")
  }

  // PaymentMethod <-> PaymentMethodDto
  fun toDto(paymentMethod: PaymentMethod): PaymentMethodDto = when (paymentMethod) {
    PaymentMethod.CreditCard -> PaymentMethodDto.CreditCard
  }

  fun fromDto(dto: PaymentMethodDto): PaymentMethod = when (dto) {
    PaymentMethodDto.CreditCard -> PaymentMethod.CreditCard
  }

  // UserSecretType <-> UserSecretTypeDto
  fun toDto(type: UserSecretType): UserSecretTypeDto = when (type) {
    UserSecretType.JWT -> UserSecretTypeDto.Jwt
    UserSecretType.SecretKey -> UserSecretTypeDto.SecretKey
  }

  fun fromDto(dto: UserSecretTypeDto): UserSecretType = when (dto) {
    UserSecretTypeDto.Jwt -> UserSecretType.JWT
    UserSecretTypeDto.SecretKey -> UserSecretType.SecretKey
  }

  // ExtractEmit <-> ScrapeEmitDto
  fun toDto(emit: ExtractEmit): ScrapeEmitDto = when (emit) {
    ExtractEmit.text -> ScrapeEmitDto.text
    ExtractEmit.html -> ScrapeEmitDto.html
    ExtractEmit.pixel -> ScrapeEmitDto.pixel
    ExtractEmit.date -> ScrapeEmitDto.date
  }

  fun fromDto(dto: ScrapeEmitDto): ExtractEmit = when (dto) {
    ScrapeEmitDto.text -> ExtractEmit.text
    ScrapeEmitDto.html -> ExtractEmit.html
    ScrapeEmitDto.pixel -> ExtractEmit.pixel
    ScrapeEmitDto.date -> ExtractEmit.date
  }

  // PuppeteerWaitUntil <-> PuppeteerWaitUntilDto
  fun toDto(waitUntil: PuppeteerWaitUntil): PuppeteerWaitUntilDto = when (waitUntil) {
    PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
    PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
    PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
  }

  fun fromDto(dto: PuppeteerWaitUntilDto): PuppeteerWaitUntil = when (dto) {
    PuppeteerWaitUntilDto.load -> PuppeteerWaitUntil.load
    PuppeteerWaitUntilDto.networkidle0 -> PuppeteerWaitUntil.networkidle0
    PuppeteerWaitUntilDto.networkidle2 -> PuppeteerWaitUntil.networkidle2
    PuppeteerWaitUntilDto.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
  }

  // ExtendContext <-> ExtendContentOptionsDto
  fun toDto(context: ExtendContext): ExtendContentOptionsDto = when (context) {
    ExtendContext.PREVIOUS -> ExtendContentOptionsDto.PREVIOUS
    ExtendContext.NEXT -> ExtendContentOptionsDto.NEXT
    ExtendContext.NONE -> ExtendContentOptionsDto.NONE
    ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptionsDto.PREVIOUS_AND_NEXT
  }

  fun fromDto(dto: ExtendContentOptionsDto?): ExtendContext = when (dto) {
    ExtendContentOptionsDto.NEXT -> ExtendContext.NEXT
    ExtendContentOptionsDto.PREVIOUS -> ExtendContext.PREVIOUS
    ExtendContentOptionsDto.PREVIOUS_AND_NEXT -> ExtendContext.PREVIOUS_AND_NEXT
    else -> ExtendContext.NONE
  }

  // RecordDateFieldDto <-> MaxAgeDaysDateField
  fun fromDto(dto: RecordDateFieldDto): MaxAgeDaysDateField = when (dto) {
    RecordDateFieldDto.createdAt -> MaxAgeDaysDateField.createdAt
    RecordDateFieldDto.startingAt -> MaxAgeDaysDateField.startingAt
    RecordDateFieldDto.publishedAt -> MaxAgeDaysDateField.publishedAt
  }

  fun toDto(field: MaxAgeDaysDateField): RecordDateFieldDto = when (field) {
    MaxAgeDaysDateField.createdAt -> RecordDateFieldDto.createdAt
    MaxAgeDaysDateField.startingAt -> RecordDateFieldDto.startingAt
    MaxAgeDaysDateField.publishedAt -> RecordDateFieldDto.publishedAt
  }

  // RecordField <-> RecordFieldDto
  fun toDto(field: RecordField): RecordFieldDto = when (field) {
    RecordField.text -> RecordFieldDto.text
    RecordField.pixel -> RecordFieldDto.pixel
    RecordField.markup -> RecordFieldDto.markup
  }

  fun fromDto(dto: RecordFieldDto): RecordField = when (dto) {
    RecordFieldDto.text -> RecordField.text
    RecordFieldDto.pixel -> RecordField.pixel
    RecordFieldDto.markup -> RecordField.markup
  }

  // StringFilterOperator <-> StringFilterOperatorDto
  fun toDto(operator: StringFilterOperator): StringFilterOperatorDto = when (operator) {
    StringFilterOperator.contains -> StringFilterOperatorDto.contains
    StringFilterOperator.matches -> StringFilterOperatorDto.matches
    StringFilterOperator.endsWith -> StringFilterOperatorDto.endsWith
    StringFilterOperator.startsWidth -> StringFilterOperatorDto.startsWidth
  }

  fun fromDto(dto: StringFilterOperatorDto): StringFilterOperator = when (dto) {
    StringFilterOperatorDto.contains -> StringFilterOperator.contains
    StringFilterOperatorDto.matches -> StringFilterOperator.matches
    StringFilterOperatorDto.endsWith -> StringFilterOperator.endsWith
    StringFilterOperatorDto.startsWidth -> StringFilterOperator.startsWidth
  }
}


