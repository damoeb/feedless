package org.migor.feedless.attachment

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CreateAttachmentFieldsInput
import org.migor.feedless.generated.types.CreateAttachmentInput
import org.migor.feedless.generated.types.DeleteAttachmentInput
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*
import org.migor.feedless.generated.types.Attachment as AttachmentDto


@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.attachment} & ${AppLayer.api}")
class AttachmentResolver(
  private val attachmentService: AttachmentService,
) {

  private val log = LoggerFactory.getLogger(AttachmentResolver::class.simpleName)

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateAttachment)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun createAttachment(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATEANNOTATION_INPUT_ARGUMENT.Data) data: CreateAttachmentInput
  ): AttachmentDto = coroutineScope {
    log.debug("createAttachment $data")
    val file: MultipartFile = dfe.getArgument<MultipartFile>("input")!!
    val attachment = data.attachment.toDomain(file)
    attachmentService.createAttachment(DocumentId(data.where.id), attachment).toDto()
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteAttachment)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun deleteAttachment(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEANNOTATION_INPUT_ARGUMENT.Data) data: DeleteAttachmentInput,
  ): Boolean = coroutineScope {
    log.debug("deleteAttachment $data")
    attachmentService.deleteAttachment(AttachmentId(data.where.id))
    true
  }
}

private fun CreateAttachmentFieldsInput.toDomain(file: MultipartFile): Attachment {
  return Attachment(
    id = AttachmentId(UUID.randomUUID()),
    name = name.ifBlank { file.originalFilename ?: "unknown" },
    hasData = true,
    remoteDataUrl = null,
    mimeType = file.contentType ?: "application/octet-stream",
    originalUrl = null,
    size = file.size,
    duration = null,
    documentId = DocumentId(),
    data = file.bytes,
  )
}

internal fun Attachment.toDto(): AttachmentDto {
  return AttachmentDto(
    id = id.uuid.toString(),
    type = mimeType,
    url = remoteDataUrl ?: "",
    size = size,
    duration = duration
  )
}
