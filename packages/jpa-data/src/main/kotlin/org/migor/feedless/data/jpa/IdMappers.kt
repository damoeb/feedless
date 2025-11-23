package org.migor.feedless.data.jpa

import org.mapstruct.Mapper
import org.migor.feedless.actions.ScrapeActionId
import org.migor.feedless.agent.AgentId
import org.migor.feedless.annotation.AnnotationId
import org.migor.feedless.attachment.AttachmentId
import org.migor.feedless.connectedApp.ConnectedAppId
import org.migor.feedless.document.DocumentId
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureValueId
import org.migor.feedless.group.GroupId
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.invoice.InvoiceId
import org.migor.feedless.license.LicenseId
import org.migor.feedless.order.OrderId
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.pipelineJob.PipelineJobId
import org.migor.feedless.plan.PlanId
import org.migor.feedless.product.PricedProductId
import org.migor.feedless.product.ProductId
import org.migor.feedless.report.ReportId
import org.migor.feedless.report.SegmentationId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.systemSettings.SystemSettingsId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.UserGroupAssignmentId
import org.migor.feedless.userSecret.UserSecretId
import java.util.*

/**
 * Shared mapper configuration for converting between UUID and ID wrapper classes
 */
@Mapper
interface IdMappers {

    // User IDs
    fun map(value: UUID?): UserId? = value?.let { UserId(it) }
    fun map(value: UserId?): UUID? = value?.uuid

    // Repository IDs
    fun mapRepositoryId(value: UUID?): RepositoryId? = value?.let { RepositoryId(it) }
    fun mapRepositoryId(value: RepositoryId?): UUID? = value?.uuid

    // Source IDs
    fun mapSourceId(value: UUID?): SourceId? = value?.let { SourceId(it) }
    fun mapSourceId(value: SourceId?): UUID? = value?.uuid

    // Document IDs
    fun mapDocumentId(value: UUID?): DocumentId? = value?.let { DocumentId(it) }
    fun mapDocumentId(value: DocumentId?): UUID? = value?.uuid

    // Agent IDs
    fun mapAgentId(value: UUID?): AgentId? = value?.let { AgentId(it) }
    fun mapAgentId(value: AgentId?): UUID? = value?.uuid

    // Attachment IDs
    fun mapAttachmentId(value: UUID?): AttachmentId? = value?.let { AttachmentId(it) }
    fun mapAttachmentId(value: AttachmentId?): UUID? = value?.uuid

    // Annotation IDs
    fun mapAnnotationId(value: UUID?): AnnotationId? = value?.let { AnnotationId(it) }
    fun mapAnnotationId(value: AnnotationId?): UUID? = value?.uuid

    // ConnectedApp IDs
    fun mapConnectedAppId(value: UUID?): ConnectedAppId? = value?.let { ConnectedAppId(it) }
    fun mapConnectedAppId(value: ConnectedAppId?): UUID? = value?.uuid

    // Feature IDs
    fun mapFeatureId(value: UUID?): FeatureId? = value?.let { FeatureId(it) }
    fun mapFeatureId(value: FeatureId?): UUID? = value?.uuid

    fun mapFeatureGroupId(value: UUID?): FeatureGroupId? = value?.let { FeatureGroupId(it) }
    fun mapFeatureGroupId(value: FeatureGroupId?): UUID? = value?.uuid

    fun mapFeatureValueId(value: UUID?): FeatureValueId? = value?.let { FeatureValueId(it) }
    fun mapFeatureValueId(value: FeatureValueId?): UUID? = value?.uuid

    // Group IDs
    fun mapGroupId(value: UUID?): GroupId? = value?.let { GroupId(it) }
    fun mapGroupId(value: GroupId?): UUID? = value?.uuid

    // Harvest IDs
    fun mapHarvestId(value: UUID?): HarvestId? = value?.let { HarvestId(it) }
    fun mapHarvestId(value: HarvestId?): UUID? = value?.uuid

    // Invoice IDs
    fun mapInvoiceId(value: UUID?): InvoiceId? = value?.let { InvoiceId(it) }
    fun mapInvoiceId(value: InvoiceId?): UUID? = value?.uuid

    // License IDs
    fun mapLicenseId(value: UUID?): LicenseId? = value?.let { LicenseId(it) }
    fun mapLicenseId(value: LicenseId?): UUID? = value?.uuid

    // Order IDs
    fun mapOrderId(value: UUID?): OrderId? = value?.let { OrderId(it) }
    fun mapOrderId(value: OrderId?): UUID? = value?.uuid

    // OneTimePassword IDs
    fun mapOneTimePasswordId(value: UUID?): OneTimePasswordId? = value?.let { OneTimePasswordId(it) }
    fun mapOneTimePasswordId(value: OneTimePasswordId?): UUID? = value?.uuid

    // PipelineJob IDs
    fun mapPipelineJobId(value: UUID?): PipelineJobId? = value?.let { PipelineJobId(it) }
    fun mapPipelineJobId(value: PipelineJobId?): UUID? = value?.uuid

    // Plan IDs
    fun mapPlanId(value: UUID?): PlanId? = value?.let { PlanId(it) }
    fun mapPlanId(value: PlanId?): UUID? = value?.uuid

    // Product IDs
    fun mapProductId(value: UUID?): ProductId? = value?.let { ProductId(it) }
    fun mapProductId(value: ProductId?): UUID? = value?.uuid

    fun mapPricedProductId(value: UUID?): PricedProductId? = value?.let { PricedProductId(it) }
    fun mapPricedProductId(value: PricedProductId?): UUID? = value?.uuid

    // Report IDs
    fun mapReportId(value: UUID?): ReportId? = value?.let { ReportId(it) }
    fun mapReportId(value: ReportId?): UUID? = value?.uuid

    fun mapSegmentationId(value: UUID?): SegmentationId? = value?.let { SegmentationId(it) }
    fun mapSegmentationId(value: SegmentationId?): UUID? = value?.uuid

    // ScrapeAction IDs
    fun mapScrapeActionId(value: UUID?): ScrapeActionId? = value?.let { ScrapeActionId(it) }
    fun mapScrapeActionId(value: ScrapeActionId?): UUID? = value?.uuid

    // SystemSettings IDs
    fun mapSystemSettingsId(value: UUID?): SystemSettingsId? = value?.let { SystemSettingsId(it) }
    fun mapSystemSettingsId(value: SystemSettingsId?): UUID? = value?.uuid

    // UserGroupAssignment IDs
    fun mapUserGroupAssignmentId(value: UUID?): UserGroupAssignmentId? = value?.let { UserGroupAssignmentId(it) }
    fun mapUserGroupAssignmentId(value: UserGroupAssignmentId?): UUID? = value?.uuid

    // UserSecret IDs
    fun mapUserSecretId(value: UUID?): UserSecretId? = value?.let { UserSecretId(it) }
    fun mapUserSecretId(value: UserSecretId?): UUID? = value?.uuid
}

