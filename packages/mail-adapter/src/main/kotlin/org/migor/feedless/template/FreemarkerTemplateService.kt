package org.migor.feedless.template

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

@Service
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
class FreemarkerTemplateService(
  private val freemarkerConfigurer: FreeMarkerConfigurer
): TemplateService {

  private val log = LoggerFactory.getLogger(FreemarkerTemplateService::class.simpleName)

  override fun <T> renderTemplate(
    template: FtlTemplate<T>,
  ): String {
    val templateName = template.templateName
    log.debug("renderTemplate $templateName")
    return ByteArrayOutputStream().use {
      freemarkerConfigurer.configuration.getTemplate("$templateName.ftl.html")
        .process(template.params, OutputStreamWriter(it))
      String(it.toByteArray())
    }
  }
}
