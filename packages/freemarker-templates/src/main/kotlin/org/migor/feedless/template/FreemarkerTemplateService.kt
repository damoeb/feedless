package org.migor.feedless.mail.template

import org.migor.feedless.AppLayer
import org.migor.feedless.template.FreemarkerTemplate
import org.migor.feedless.template.TemplateService
import org.migor.feedless.template.TemplateVariant
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.OutputStreamWriter

@Service
@Profile(AppLayer.service)
class FreemarkerTemplateService(
  private val freemarkerConfigurer: FreeMarkerConfigurer
) : TemplateService {

  private val log = LoggerFactory.getLogger(FreemarkerTemplateService::class.simpleName)

  override fun <T> renderTemplate(template: FreemarkerTemplate<T>): String =
    renderTemplate(template, null)

  override fun <T> renderTemplate(
    template: FreemarkerTemplate<T>,
    variant: TemplateVariant?,
  ): String {
    val templateName = resolveTemplateName(template.templateName, variant)
    log.debug("renderTemplate $templateName")
    return ByteArrayOutputStream().use {
      freemarkerConfigurer.configuration.getTemplate("$templateName.ftl.html")
        .process(template.params, OutputStreamWriter(it))
      String(it.toByteArray())
    }
  }

  /**
   * Bevorzugt die produktspezifische Vorlage, fällt auf die allgemeine zurück.
   * So bleibt der Versandpfad generisch: er kennt nur einen Variantennamen und
   * kein Produkt.
   */
  private fun resolveTemplateName(name: String, variant: TemplateVariant?): String {
    if (variant == null) {
      return name
    }
    val specific = "$name-${variant.value}"
    return if (templateExists(specific)) {
      specific
    } else {
      log.debug("no template $specific, falling back to $name")
      name
    }
  }

  private fun templateExists(name: String): Boolean = try {
    freemarkerConfigurer.configuration.getTemplate("$name.ftl.html")
    true
  } catch (e: FileNotFoundException) {
    false
  }
}
