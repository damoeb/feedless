package org.migor.feedless.common

import org.apache.pdfbox.pdmodel.PDDocument
import org.fit.pdfdom.PDFDomTree
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.nio.charset.StandardCharsets


@Service
@Transactional(propagation = Propagation.NEVER)
class PdfService {

  private val log = LoggerFactory.getLogger(PdfService::class.simpleName)

  suspend fun toHTML(file: File): String {
    file.inputStream().use { inputStream ->
      return toHTML(inputStream)
    }
  }

  private suspend fun toHTML(inputStream: InputStream): String {
    PDDocument.load(inputStream).use { pdf ->
      val parser = PDFDomTree()
      ByteArrayOutputStream().use { baos ->
        PrintWriter(baos, true, StandardCharsets.UTF_8).use { output ->
          parser.writeText(pdf, output)
        }
        return String(baos.toByteArray(), StandardCharsets.UTF_8)
      }
    }
  }

}
