package org.migor.feedless.classifier

import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClass
import org.migor.feedless.document.DocumentClassifier

class TensorflowDocumentClassifier : DocumentClassifier {
  override suspend fun classify(document: Document): DocumentClass {
    TODO("Not yet implemented")
  }
}
