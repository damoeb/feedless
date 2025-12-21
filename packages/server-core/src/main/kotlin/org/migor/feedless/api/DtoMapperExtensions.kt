package org.migor.feedless.api

import org.migor.feedless.api.mapper.DocumentMapper
import org.migor.feedless.api.mapper.FeatureMapper
import org.migor.feedless.api.mapper.ScrapeResponseMapper
import org.migor.feedless.api.mapper.UserSecretMapper
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.mapper.toSource
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.data.jpa.featureValue.FeatureValueEntity
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.feed.discovery.RemoteNativeFeedRef
import org.migor.feedless.scrape.GenericFeedRule
import org.migor.feedless.scrape.GenericFeedSelectors
import org.migor.feedless.source.Source
import org.migor.feedless.userSecret.UserSecret
import org.springframework.stereotype.Component
import org.migor.feedless.api.mapper.fromDto as scrapeFlowFromDto
import org.migor.feedless.generated.types.Feature as FeatureDto
import org.migor.feedless.generated.types.FeatureGroup as FeatureGroupDto
import org.migor.feedless.generated.types.FeatureValue as FeatureValueDto
import org.migor.feedless.generated.types.MimeData as MimeDataDto
import org.migor.feedless.generated.types.MimeDataInput as MimeDataInputDto
import org.migor.feedless.generated.types.Record as RecordDto
import org.migor.feedless.generated.types.RemoteNativeFeed as RemoteNativeFeedDto
import org.migor.feedless.generated.types.ScrapeAction as ScrapeActionDto
import org.migor.feedless.generated.types.ScrapeFlowInput as ScrapeFlowInputDto
import org.migor.feedless.generated.types.ScrapeResponse as ScrapeResponseDto
import org.migor.feedless.generated.types.ScrapeResponseInput as ScrapeResponseInputDto
import org.migor.feedless.generated.types.Selectors as SelectorsDto
import org.migor.feedless.generated.types.SelectorsInput as SelectorsInputDto
import org.migor.feedless.generated.types.Source as SourceDto
import org.migor.feedless.generated.types.SourceInput as SourceInputDto
import org.migor.feedless.generated.types.TextData as TextDataDto
import org.migor.feedless.generated.types.TextDataInput as TextDataInputDto
import org.migor.feedless.generated.types.TransientGenericFeed as TransientGenericFeedDto
import org.migor.feedless.generated.types.UserSecret as UserSecretDto

/**
 * Facade component that provides extension function API using MapStruct mappers
 */
@Component
class DtoMapperFacade(
//    private val enumMapper: EnumMapper,
//    private val userMapper: UserMapper,
  private val documentMapper: DocumentMapper,
//    private val productMapper: ProductMapper,
  private val featureMapper: FeatureMapper,
  private val userSecretMapper: UserSecretMapper,
//    private val repositoryMapper: RepositoryMapper,
  private val scrapeResponseMapper: ScrapeResponseMapper
) {

  companion object {
    private lateinit var instance: DtoMapperFacade

    fun initialize(facade: DtoMapperFacade) {
      instance = facade
    }

    fun getInstance(): DtoMapperFacade = instance
  }

  init {
    initialize(this)
  }

  // Enum mappings
//  fun toDto(visibility: EntityVisibility): VisibilityDto = enumMapper.toDto(visibility)
//  fun fromDto(dto: VisibilityDto): EntityVisibility = enumMapper.fromDto(dto)
//
//  fun toDto(vertical: Vertical): VerticalDto = enumMapper.toDto(vertical)
//  fun fromDto(dto: VerticalDto): Vertical = enumMapper.fromDto(dto)
//
//  fun toDto(paymentMethod: PaymentMethod): PaymentMethodDto = enumMapper.toDto(paymentMethod)
//  fun fromDto(dto: PaymentMethodDto): PaymentMethod = enumMapper.fromDto(dto)
//
//  fun toDto(type: UserSecretType): UserSecretTypeDto = enumMapper.toDto(type)
//  fun fromDto(dto: UserSecretTypeDto): UserSecretType = enumMapper.fromDto(dto)
//
//  fun toDto(emit: ExtractEmit): ScrapeEmitDto = enumMapper.toDto(emit)
//  fun fromDto(dto: ScrapeEmitDto): ExtractEmit = enumMapper.fromDto(dto)
//
//  fun fromDto(dto: RecordDateFieldDto): MaxAgeDaysDateField = enumMapper.fromDto(dto)
//  fun toDto(field: MaxAgeDaysDateField): RecordDateFieldDto = enumMapper.toDto(field)
//
//  fun toDto(field: RecordField): RecordFieldDto = enumMapper.toDto(field)
//  fun fromDto(dto: RecordFieldDto): RecordField = enumMapper.fromDto(dto)
//
//  fun toDto(operator: StringFilterOperator): StringFilterOperatorDto =
//    enumMapper.toDto(operator)
//  fun fromDto(dto: StringFilterOperatorDto): StringFilterOperator =
//    enumMapper.fromDto(dto)

  // Domain object mappings
//    fun toDto(user: User): UserDto = userMapper.toDto(user)

  fun toDto(document: Document, propertyService: PropertyService): RecordDto =
    documentMapper.toDto(document, propertyService)

//    fun toDto(product: Product): ProductDto = productMapper.toDto(product)
//    fun toDto(pricedProduct: PricedProduct): PricedProductDto = productMapper.toDto(pricedProduct)

  fun toDto(entity: FeatureGroupEntity, features: List<FeatureDto>): FeatureGroupDto =
    featureMapper.toDto(entity, features)

  fun toDto(entity: FeatureValueEntity): FeatureValueDto =
    featureMapper.toDto(entity)

  fun toDto(userSecret: UserSecret, mask: Boolean = true): UserSecretDto =
    userSecretMapper.toDto(userSecret, mask)


  fun toDto(source: Source): SourceDto = source.toDto()
  fun fromDto(input: SourceInputDto): Source = input.toSource()

  fun toDto(action: org.migor.feedless.actions.ScrapeAction): ScrapeActionDto =
    action.toDto()

  fun fromDto(input: ScrapeFlowInputDto): MutableList<org.migor.feedless.actions.ScrapeAction> =
    input.scrapeFlowFromDto()

  // Scrape response mappings
  fun fromDto(input: ScrapeResponseInputDto): ScrapeResponseDto = scrapeResponseMapper.fromDto(input)
  fun fromDto(input: MimeDataInputDto): MimeDataDto = scrapeResponseMapper.fromDto(input)
  fun fromDto(input: TextDataInputDto): TextDataDto = scrapeResponseMapper.fromDto(input)
  fun toDto(ref: RemoteNativeFeedRef): RemoteNativeFeedDto = scrapeResponseMapper.toDto(ref)
  fun toDto(rule: GenericFeedRule): TransientGenericFeedDto = scrapeResponseMapper.toDto(rule)
  fun fromDto(input: SelectorsDto): GenericFeedSelectors = scrapeResponseMapper.fromDto(input)
}

// Extension functions that delegate to the facade
//fun EntityVisibility.toDto(): VisibilityDto = DtoMapperFacade.getInstance().toDto(this)
//fun VisibilityDto.fromDto(): EntityVisibility = DtoMapperFacade.getInstance().fromDto(this)
//
//fun Vertical.toDto(): VerticalDto = DtoMapperFacade.getInstance().toDto(this)
//fun VerticalDto.fromDto(): Vertical = DtoMapperFacade.getInstance().fromDto(this)
//
//fun PaymentMethod.toDTO(): PaymentMethodDto = DtoMapperFacade.getInstance().toDto(this)
//fun PaymentMethodDto.fromDto(): PaymentMethod = DtoMapperFacade.getInstance().fromDto(this)
//
//fun UserSecretType.toDto(): UserSecretTypeDto = DtoMapperFacade.getInstance().toDto(this)
//fun UserSecretTypeDto.fromDto(): UserSecretType = DtoMapperFacade.getInstance().fromDto(this)
//
//fun ExtractEmit.toDto(): ScrapeEmitDto = DtoMapperFacade.getInstance().toDto(this)
//fun ScrapeEmitDto.fromDto(): ExtractEmit = DtoMapperFacade.getInstance().fromDto(this)
//
//fun RecordDateFieldDto.fromDto(): MaxAgeDaysDateField = DtoMapperFacade.getInstance().fromDto(this)
//fun MaxAgeDaysDateField.toDto(): RecordDateFieldDto = DtoMapperFacade.getInstance().toDto(this)
//
//fun RecordField.toDto(): RecordFieldDto =
//  DtoMapperFacade.getInstance().toDto(this)
//fun RecordFieldDto.fromDto(): RecordField =
//  DtoMapperFacade.getInstance().fromDto(this)
//
//fun StringFilterOperator.toDto(): StringFilterOperatorDto =
//  DtoMapperFacade.getInstance().toDto(this)
//fun StringFilterOperatorDto.fromDto(): StringFilterOperator =
//  DtoMapperFacade.getInstance().fromDto(this)

//fun User.toDTO(): UserDto = DtoMapperFacade.getInstance().toDto(this)

fun Document.toDto(propertyService: PropertyService): RecordDto =
  DtoMapperFacade.getInstance().toDto(this, propertyService)

//fun Product.toDTO(): ProductDto = DtoMapperFacade.getInstance().toDto(this)
//fun PricedProduct.toDto(): PricedProductDto = DtoMapperFacade.getInstance().toDto(this)

fun FeatureGroupEntity.toDto(features: List<FeatureDto>): FeatureGroupDto =
  DtoMapperFacade.getInstance().toDto(this, features)

fun FeatureValueEntity.toDto(): FeatureValueDto =
  DtoMapperFacade.getInstance().toDto(this)

fun UserSecret.toDto(mask: Boolean = true): UserSecretDto =
  DtoMapperFacade.getInstance().toDto(this, mask)

// Source and ScrapeAction extension functions are now directly defined in their mapper files

fun ScrapeResponseInputDto.fromDto(): ScrapeResponseDto = DtoMapperFacade.getInstance().fromDto(this)
fun MimeDataInputDto.fromDto(): MimeDataDto = DtoMapperFacade.getInstance().fromDto(this)
fun TextDataInputDto.fromDto(): TextDataDto = DtoMapperFacade.getInstance().fromDto(this)
fun RemoteNativeFeedRef.toDto(): RemoteNativeFeedDto = DtoMapperFacade.getInstance().toDto(this)
fun GenericFeedRule.toDto(): TransientGenericFeedDto = DtoMapperFacade.getInstance().toDto(this)
fun SelectorsDto.fromDto(): GenericFeedSelectors = DtoMapperFacade.getInstance().fromDto(this)

fun createDocumentUrl(propertyService: PropertyService, id: DocumentId): String =
  "${propertyService.apiGatewayUrl}/article/${id}"

// Helper function for isHtml
fun isHtml(rawMimeType: String?): Boolean = rawMimeType?.lowercase()?.startsWith("text/html") == true

fun SelectorsInputDto.fromDto(): SelectorsDto = SelectorsDto(
  contextXPath = contextXPath,
  linkXPath = linkXPath,
  dateXPath = dateXPath,
  extendContext = extendContext,
  dateIsStartOfEvent = org.apache.commons.lang3.BooleanUtils.isTrue(dateIsStartOfEvent),
  paginationXPath = paginationXPath,
)


