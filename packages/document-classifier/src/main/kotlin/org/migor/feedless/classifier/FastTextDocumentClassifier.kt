package org.migor.feedless.classifier

import com.github.jfasttext.JFastText
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClass
import org.migor.feedless.document.DocumentClassifier
import org.migor.feedless.document.DocumentClassifierModel
import org.slf4j.LoggerFactory


class FastTextDocumentClassifier : DocumentClassifier {

  private val log = LoggerFactory.getLogger(FastTextDocumentClassifier::class.java)

  override suspend fun classify(document: Document, model: DocumentClassifierModel): List<DocumentClass> {
    val ft = JFastText()
    ft.loadModel(model.model)

    val text = "${document.title} ${document.text}"
    return ft.predictProba(text, 3).map { DocumentClass(it.label, it.logProb.toDouble()) }
  }
}
