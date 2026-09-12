package org.migor.feedless.api.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.generated.types.NullableIntUpdateOperationsInput
import org.migor.feedless.generated.types.NullableLongUpdateOperationsInput
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.RecordDateFieldUpdateOperationsInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.RetentionUpdateInput
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.util.toLocalDateTime

class RepositoryCommandMapperTest {

  @Test
  fun `retention maxAgeDays clear maps to clear flag`() {
    val input = RepositoryUpdateDataInput(
      retention = RetentionUpdateInput(
        maxAgeDays = NullableIntUpdateOperationsInput(set = null),
      ),
    )

    val domain = input.toDomain()

    assertThat(domain.clearRetentionMaxAgeDays).isTrue()
    assertThat(domain.retentionMaxAgeDays).isNull()
  }

  @Test
  fun `retention maxCapacity clear maps to clear flag`() {
    val input = RepositoryUpdateDataInput(
      retention = RetentionUpdateInput(
        maxCapacity = NullableIntUpdateOperationsInput(set = null),
      ),
    )

    val domain = input.toDomain()

    assertThat(domain.clearRetentionMaxCapacity).isTrue()
    assertThat(domain.retentionMaxCapacity).isNull()
  }

  @Test
  fun `nextUpdateAt null set maps to schedule now flag`() {
    val input = RepositoryUpdateDataInput(
      nextUpdateAt = NullableLongUpdateOperationsInput(set = null),
    )

    val domain = input.toDomain()

    assertThat(domain.scheduleNextUpdateNow).isTrue()
    assertThat(domain.nextUpdateAt).isNull()
  }

  @Test
  fun `retention ageReferenceField maps to domain field`() {
    val input = RepositoryUpdateDataInput(
      retention = RetentionUpdateInput(
        ageReferenceField = RecordDateFieldUpdateOperationsInput(set = RecordDateField.publishedAt),
      ),
    )

    val domain = input.toDomain()

    assertThat(domain.retentionMaxAgeDaysReferenceField).isEqualTo(MaxAgeDaysDateField.publishedAt)
    assertThat(domain.clearRetentionMaxAgeDays).isFalse()
    assertThat(domain.clearRetentionMaxCapacity).isFalse()
  }

  @Test
  fun `retention maxAgeDays set maps value without clear flag`() {
    val input = RepositoryUpdateDataInput(
      retention = RetentionUpdateInput(
        maxAgeDays = NullableIntUpdateOperationsInput(set = 14),
      ),
    )

    val domain = input.toDomain()

    assertThat(domain.retentionMaxAgeDays).isEqualTo(14)
    assertThat(domain.clearRetentionMaxAgeDays).isFalse()
  }

  @Test
  fun `nextUpdateAt set maps to domain datetime`() {
    val epochMillis = 1_700_000_000_000L
    val input = RepositoryUpdateDataInput(
      nextUpdateAt = NullableLongUpdateOperationsInput(set = epochMillis),
    )

    val domain = input.toDomain()

    assertThat(domain.nextUpdateAt).isEqualTo(epochMillis.toLocalDateTime())
    assertThat(domain.scheduleNextUpdateNow).isFalse()
  }
}
