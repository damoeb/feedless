package org.migor.feedless.report

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles("test", "database", AppProfiles.report, AppLayer.repository)
@Testcontainers
class ReportRecipientRepositoryIntTest {

  @Autowired
  private lateinit var reportRecipientRepository: ReportRecipientRepository

  private fun uniqueEmail() = "hans-${System.nanoTime()}@example.com"

  @Test
  fun `finds a recipient by id and by address`() {
    val saved = reportRecipientRepository.save(ReportRecipient(email = uniqueEmail()))

    assertThat(reportRecipientRepository.findById(saved.id)?.email).isEqualTo(saved.email)
    assertThat(reportRecipientRepository.findByEmail(saved.email)?.id).isEqualTo(saved.id)
  }

  @Test
  fun `keeps the opt-in flag`() {
    val saved = reportRecipientRepository.save(ReportRecipient(email = uniqueEmail(), optInRequired = true))

    assertThat(reportRecipientRepository.findById(saved.id)!!.optInRequired).isTrue()
  }

  @Test
  fun `refuses a second row for the same address`() {
    val email = uniqueEmail()
    reportRecipientRepository.save(ReportRecipient(email = email))

    assertThatThrownBy { reportRecipientRepository.save(ReportRecipient(email = email)) }
      .isInstanceOf(DataIntegrityViolationException::class.java)
  }
}
