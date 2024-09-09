package org.migor.feedless.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.generated.types.Cursor

class RepositoryResolverKtTest {

  @ParameterizedTest
  @CsvSource(
    value = [
      "0, 10, 0, 10",
      "1, 10, 1, 10",
      "-1, 10, 0, 10",
      "-1, 100, 0, 10",
      "-1, -1, 0, 1",
    ]
  )
  fun testHandleCursor0(pageIn: Int, pageSizeIn: Int, expectedPage: Int, expectedPageSize: Int) {
    val (pageNumber, pageSize) = handleCursor(Cursor(
      page = pageIn,
      pageSize = pageSizeIn,
    ))
    assertThat(pageNumber).isEqualTo(expectedPage)
    assertThat(pageSize).isEqualTo(expectedPageSize)
  }
}
