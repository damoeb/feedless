package org.migor.feedless.common

import org.apache.pdfbox.Loader
import org.apache.pdfbox.tools.PDFText2HTML
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream


@Service
class PdfService {

  private val log = LoggerFactory.getLogger(PdfService::class.simpleName)

  suspend fun toHTML(file: File): String {
    file.inputStream().use { inputStream ->
      return toHTML(inputStream)
    }
  }

  private suspend fun toHTML(inputStream: InputStream): String {
    val outputStream = ByteArrayOutputStream()
    inputStream.use { input ->
      outputStream.use { output ->
        input.copyTo(output)
      }
    }
    val byteArray = outputStream.toByteArray()
    return Loader.loadPDF(byteArray).use { pdf ->
      val text2HTML = PDFText2HTML()
      text2HTML.getText(pdf)
//      val textStripper = PDFTextStripper()
//      textStripper.getText(pdf);
    }
  }

}
