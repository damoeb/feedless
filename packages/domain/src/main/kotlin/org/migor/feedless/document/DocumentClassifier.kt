package org.migor.feedless.document

interface DocumentClassifier {
  suspend fun classify(document: Document, model: DocumentClassifierModel): List<DocumentClass>
}
