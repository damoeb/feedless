package org.migor.feedless.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.user.UserId
import org.migor.feedless.util.toMillis
import java.time.LocalDateTime
import org.migor.feedless.generated.types.Report as ReportDto

class ReportResolverTest {

    @Test
    fun testDto() {
        val reportId = ReportId()
        val segmentId = SegmentationId()
        val userId = UserId()
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")
        val authorizedAt = LocalDateTime.parse("2020-01-03T08:00:00")
        val lastReportedAt = LocalDateTime.parse("2020-01-10T12:00:00")
        val nextReportedAt = LocalDateTime.parse("2020-01-11T12:00:00")
        val lastRequestedAuthorization = LocalDateTime.parse("2020-01-02T14:00:00")

        val incoming = Report(
            id = reportId,
            recipientEmail = "user@example.com",
            recipientName = "John Doe",
            authorized = true,
            authorizationAttempt = 1,
            lastRequestedAuthorization = lastRequestedAuthorization,
            authorizedAt = authorizedAt,
            lastReportedAt = lastReportedAt,
            disabled = false,
            disabledAt = null,
            nextReportedAt = nextReportedAt,
            segmentId = segmentId,
            userId = userId,
            createdAt = createdAt
        )

        assertThat(incoming.toDto()).isEqualTo(
            ReportDto(
                id = reportId.uuid.toString(),
                createdAt = createdAt.toMillis(),
            )
        )
    }


    @Test
    @Disabled
    fun `report can be created without authorization`() {
        // todo test
    }

    @Test
    @Disabled
    fun `report can be deleted without authorization`() {
        // todo test
    }
}
