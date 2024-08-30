package org.migor.feedless.community

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Tag("nlp")
class LanguageServiceTest {

  private lateinit var service: LanguageService

  @BeforeEach
  fun setUp() {
    service = LanguageService()
    service.postConstruct()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "eng;; The PGP signature can be verified using PGP or GPG. First download the KEYS as well as the asc signature file for the relevant distribution",
      "fra;; En physique, la cinématique (du grec kinêma, le mouvement) est l'étude des mouvements indépendamment des causes qui les produisent, ou, plus exactement, l'étude de tous les mouvements possibles.",
      "deu;; Nach dem Tode des Vaters, der zu den Gründervätern der Vereinigten Staaten zählt, trat er mit Unterstützung von Gouverneur Richard Henry Lee mit einem Offizierspatent in die neu gegründete United States Army ein. "
    ],
    delimiterString = ";; "
  )
  fun `given a text fragment in a specific language nlp will detect it`(exptectedLang: String, input: String) =
    runTest {
      assertThat(service.bestLanguage(input).lang).isEqualTo(exptectedLang)
    }
}
