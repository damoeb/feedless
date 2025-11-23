package org.migor.feedless.api

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.pipeline.plugins.RecordField
import org.migor.feedless.pipeline.plugins.StringFilterOperator
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.scrape.ExtendContext
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.userSecret.UserSecretType
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

// Extension functions that delegate to the facade
fun EntityVisibility.toDto(): VisibilityDto = when (this) {
    EntityVisibility.isPrivate -> VisibilityDto.isPrivate
    EntityVisibility.isPublic -> VisibilityDto.isPublic
}

fun VisibilityDto.fromDto(): EntityVisibility = when (this) {
    VisibilityDto.isPrivate -> EntityVisibility.isPrivate
    VisibilityDto.isPublic -> EntityVisibility.isPublic
}

fun Vertical.toDto(): VerticalDto = when (this) {
    Vertical.all -> VerticalDto.all
    Vertical.visualDiff -> VerticalDto.visualDiff
    Vertical.rssProxy -> VerticalDto.rssProxy
    Vertical.reader -> VerticalDto.reader
    Vertical.feedless -> VerticalDto.feedless
    Vertical.feedDump -> VerticalDto.feedDump
    Vertical.untoldNotes -> VerticalDto.untoldNotes
    Vertical.upcoming -> VerticalDto.upcoming
    else -> throw IllegalArgumentException("$this is not a valid vertical")
}

fun VerticalDto.fromDto(): Vertical = when (this) {
    VerticalDto.all -> Vertical.all
    VerticalDto.visualDiff -> Vertical.visualDiff
    VerticalDto.rssProxy -> Vertical.rssProxy
    VerticalDto.reader -> Vertical.reader
    VerticalDto.feedless -> Vertical.feedless
    VerticalDto.feedDump -> Vertical.feedDump
    VerticalDto.untoldNotes -> Vertical.untoldNotes
    VerticalDto.upcoming -> Vertical.upcoming
    else -> throw IllegalArgumentException("$this is not a valid vertical")
}

fun PaymentMethod.toDTO(): PaymentMethodDto = when (this) {
    PaymentMethod.CreditCard -> PaymentMethodDto.CreditCard
}

fun PaymentMethodDto.fromDto(): PaymentMethod = when (this) {
    PaymentMethodDto.CreditCard -> PaymentMethod.CreditCard
}

fun UserSecretType.toDto(): UserSecretTypeDto = when (this) {
    UserSecretType.JWT -> UserSecretTypeDto.Jwt
    UserSecretType.SecretKey -> UserSecretTypeDto.SecretKey
}

fun UserSecretTypeDto.fromDto(): UserSecretType = when (this) {
    UserSecretTypeDto.Jwt -> UserSecretType.JWT
    UserSecretTypeDto.SecretKey -> UserSecretType.SecretKey
}

fun ExtractEmit.toDto(): ScrapeEmitDto = when (this) {
    ExtractEmit.text -> ScrapeEmitDto.text
    ExtractEmit.html -> ScrapeEmitDto.html
    ExtractEmit.pixel -> ScrapeEmitDto.pixel
    ExtractEmit.date -> ScrapeEmitDto.date
}

fun ScrapeEmitDto.fromDto(): ExtractEmit = when (this) {
    ScrapeEmitDto.text -> ExtractEmit.text
    ScrapeEmitDto.html -> ExtractEmit.html
    ScrapeEmitDto.pixel -> ExtractEmit.pixel
    ScrapeEmitDto.date -> ExtractEmit.date
}

fun RecordDateFieldDto.fromDto(): MaxAgeDaysDateField = when (this) {
    RecordDateFieldDto.createdAt -> MaxAgeDaysDateField.createdAt
    RecordDateFieldDto.startingAt -> MaxAgeDaysDateField.startingAt
    RecordDateFieldDto.publishedAt -> MaxAgeDaysDateField.publishedAt
}

fun MaxAgeDaysDateField.toDto(): RecordDateFieldDto = when (this) {
    MaxAgeDaysDateField.createdAt -> RecordDateFieldDto.createdAt
    MaxAgeDaysDateField.startingAt -> RecordDateFieldDto.startingAt
    MaxAgeDaysDateField.publishedAt -> RecordDateFieldDto.publishedAt
}

fun RecordField.toDto(): RecordFieldDto =
    when (this) {
        RecordField.text -> RecordFieldDto.text
        RecordField.pixel -> RecordFieldDto.pixel
        RecordField.markup -> RecordFieldDto.markup
    }

fun RecordFieldDto.fromDto(): RecordField =
    when (this) {
        RecordFieldDto.text -> RecordField.text
        RecordFieldDto.pixel -> RecordField.pixel
        RecordFieldDto.markup -> RecordField.markup
    }

fun StringFilterOperator.toDto(): StringFilterOperatorDto =
    when (this) {
        StringFilterOperator.contains -> StringFilterOperatorDto.contains
        StringFilterOperator.matches -> StringFilterOperatorDto.matches
        StringFilterOperator.endsWith -> StringFilterOperatorDto.endsWith
        StringFilterOperator.startsWidth -> StringFilterOperatorDto.startsWidth
    }

fun StringFilterOperatorDto.fromDto(): StringFilterOperator =
    when (this) {
        StringFilterOperatorDto.contains -> StringFilterOperator.contains
        StringFilterOperatorDto.matches -> StringFilterOperator.matches
        StringFilterOperatorDto.endsWith -> StringFilterOperator.endsWith
        StringFilterOperatorDto.startsWidth -> StringFilterOperator.startsWidth
    }

// PuppeteerWaitUntil <-> PuppeteerWaitUntilDto
fun PuppeteerWaitUntil.toDto(): PuppeteerWaitUntilDto = when (this) {
    PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
    PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
    PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
}

fun PuppeteerWaitUntilDto.fromDto(): PuppeteerWaitUntil = when (this) {
    PuppeteerWaitUntilDto.load -> PuppeteerWaitUntil.load
    PuppeteerWaitUntilDto.networkidle0 -> PuppeteerWaitUntil.networkidle0
    PuppeteerWaitUntilDto.networkidle2 -> PuppeteerWaitUntil.networkidle2
    PuppeteerWaitUntilDto.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
}

// ExtendContext <-> ExtendContentOptionsDto
fun ExtendContext.toDto(): ExtendContentOptionsDto = when (this) {
    ExtendContext.PREVIOUS -> ExtendContentOptionsDto.PREVIOUS
    ExtendContext.NEXT -> ExtendContentOptionsDto.NEXT
    ExtendContext.NONE -> ExtendContentOptionsDto.NONE
    ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptionsDto.PREVIOUS_AND_NEXT
}

fun ExtendContentOptionsDto.fromDto(): ExtendContext = when (this) {
    ExtendContentOptionsDto.NEXT -> ExtendContext.NEXT
    ExtendContentOptionsDto.PREVIOUS -> ExtendContext.PREVIOUS
    ExtendContentOptionsDto.PREVIOUS_AND_NEXT -> ExtendContext.PREVIOUS_AND_NEXT
    else -> ExtendContext.NONE
}
