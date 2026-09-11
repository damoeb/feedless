package org.migor.feedless.classifier

import com.github.jfasttext.JFastText
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClassifierModel
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.repository.RepositoryId
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class FastTextDocumentClassifierTest {

  companion object {
    @TempDir
    @JvmStatic
    lateinit var tempDir: Path

    private lateinit var modelPath: String

    /**
     * Baut ein winziges echtes Modell, statt einen Pfad zu erfinden. Der
     * frühere Test übergab "test" und scheiterte an "Model file doesn't
     * exist!" - er hat nie etwas klassifiziert.
     */
    @BeforeAll
    @JvmStatic
    fun trainModel() {
      val trainingData = tempDir.resolve("train.txt")
      Files.write(
        trainingData,
        listOf(
          "__label__sport Fussballturnier auf dem Sportplatz mit mehreren Mannschaften",
          "__label__sport Volleyball Training in der Turnhalle fuer alle Altersgruppen",
          "__label__sport Laufgruppe trifft sich zum gemeinsamen Joggen im Wald",
          "__label__sport Schwimmkurs im Hallenbad mit erfahrenen Trainern",
          "__label__musik Konzert des Musikvereins in der Kirche mit Blasmusik",
          "__label__musik Chorabend mit Liedern aus verschiedenen Jahrhunderten",
          "__label__musik Jazzabend im Kulturkeller mit einer lokalen Band",
          "__label__musik Platzkonzert der Harmonie auf dem Dorfplatz",
        ),
      )
      val modelBase = tempDir.resolve("model").toString()
      JFastText().runCmd(
        arrayOf(
          "supervised",
          "-input", trainingData.toString(),
          "-output", modelBase,
          "-epoch", "50",
          "-dim", "10",
          "-minCount", "1",
        ),
      )
      modelPath = "$modelBase.bin"
    }
  }

  private fun documentWith(title: String, text: String) = Document(
    url = "https://example.com/event",
    title = title,
    text = text,
    repositoryId = RepositoryId(),
    status = ReleaseStatus.released,
    contentHash = UUID.randomUUID().toString(),
  )

  @Test
  fun `should classify document`() = runTest {
    val classifier = FastTextDocumentClassifier()

    val results = classifier.classify(
      documentWith("Fussballturnier", "Turnier auf dem Sportplatz mit Mannschaften"),
      DocumentClassifierModel(modelPath),
    )

    assertThat(results).isNotEmpty()
    assertThat(results.first().category).isEqualTo("sport")
  }

  /**
   * fastText gibt Labels in seinem Ablageformat zurück, also als
   * "__label__sport". Das ist ein Detail des Dateiformats und hat in der
   * Domäne nichts zu suchen.
   */
  @Test
  fun `strips the fasttext label prefix`() = runTest {
    val classifier = FastTextDocumentClassifier()

    val results = classifier.classify(
      documentWith("Fussballturnier", "Turnier auf dem Sportplatz"),
      DocumentClassifierModel(modelPath),
    )

    assertThat(results).isNotEmpty()
    assertThat(results).noneMatch { it.category.startsWith("__label__") }
    assertThat(results.map { it.category }).containsAnyOf("sport", "musik")
  }

  /**
   * predictProba liefert eine Log-Wahrscheinlichkeit, also einen Wert <= 0.
   * Der wurde bisher ungewandelt in DocumentClass.probability geschrieben.
   */
  @Test
  fun `reports a probability, not a log probability`() = runTest {
    val classifier = FastTextDocumentClassifier()

    val results = classifier.classify(
      documentWith("Konzert", "Konzert des Musikvereins in der Kirche"),
      DocumentClassifierModel(modelPath),
    )

    assertThat(results).allSatisfy {
      assertThat(it.probability).isBetween(0.0, 1.0)
    }
  }

  @Test
  fun `loads the model once across many classifications`() = runTest {
    val loads = AtomicInteger()
    val classifier = FastTextDocumentClassifier { path ->
      loads.incrementAndGet()
      JFastText().apply { loadModel(path) }
    }

    repeat(5) {
      classifier.classify(
        documentWith("Konzert", "Konzert des Musikvereins"),
        DocumentClassifierModel(modelPath),
      )
    }

    assertThat(loads.get()).isEqualTo(1)
  }
}
