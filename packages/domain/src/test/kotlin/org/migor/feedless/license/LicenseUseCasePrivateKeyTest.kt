package org.migor.feedless.license

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.migor.feedless.AppProfiles
import org.springframework.core.env.StandardEnvironment
import java.nio.file.NoSuchFileException

class LicenseUseCasePrivateKeyTest {

  private fun licenseUseCase(pemFile: String, vararg profiles: String) = LicenseUseCase().apply {
    environment = StandardEnvironment().apply { setActiveProfiles(*profiles) }
    buildTimestamp = (System.currentTimeMillis() - 60_000).toString()
    this.pemFile = pemFile
  }

  @Test
  fun `dev starts without the private key file`() {
    val useCase = licenseUseCase("./does-not-exist.pem", "saas", AppProfiles.DEV_ONLY)

    assertDoesNotThrow { useCase.onInit() }
    assertThat(useCase.feedlessPrivateKey).isNull()
  }

  @Test
  fun `dev starts without APP_PEM_FILE`() {
    val useCase = licenseUseCase("", "saas", AppProfiles.DEV_ONLY)

    assertDoesNotThrow { useCase.onInit() }
    assertThat(useCase.feedlessPrivateKey).isNull()
  }

  @Test
  fun `outside dev the private key file is still required`() {
    val useCase = licenseUseCase("./does-not-exist.pem", "saas")

    assertThrows<NoSuchFileException> { useCase.onInit() }
  }
}
