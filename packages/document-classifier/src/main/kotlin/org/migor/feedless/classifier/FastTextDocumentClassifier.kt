package org.migor.feedless.classifier

import com.github.jfasttext.JFastText
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClass
import org.migor.feedless.document.DocumentClassifier
import org.migor.feedless.document.DocumentClassifierModel
import org.slf4j.LoggerFactory
import kotlin.math.exp

private const val MAX_PREDICTIONS = 3
private const val FASTTEXT_LABEL_PREFIX = "__label__"

private fun loadFastText(path: String): JFastText {
  val ft = JFastText()
  ft.loadModel(path)
  return ft
}

class FastTextDocumentClassifier(
  loader: (String) -> JFastText = ::loadFastText,
) : DocumentClassifier {

  private val log = LoggerFactory.getLogger(FastTextDocumentClassifier::class.java)

  private val models = ModelCache { path ->
    log.info("loading fasttext model $path")
    loader(path)
  }

  override suspend fun classify(
    document: Document,
    model: DocumentClassifierModel,
  ): List<DocumentClass> {
    val text = "${document.title} ${document.text}"
    return models.withModel(model.model) { ft ->
      ft.predictProba(text, MAX_PREDICTIONS).map {
        DocumentClass(
          // fastText gibt das Label in seinem Ablageformat zurück, also mit
          // dem Präfix "__label__". Das ist ein Detail des Dateiformats und
          // gehört nicht in die Domäne.
          it.label.removePrefix(FASTTEXT_LABEL_PREFIX),
          // predictProba liefert eine Log-Wahrscheinlichkeit, also einen Wert
          // <= 0. DocumentClass.probability erwartet eine Wahrscheinlichkeit.
          exp(it.logProb.toDouble()),
        )
      }
    }
  }
}
