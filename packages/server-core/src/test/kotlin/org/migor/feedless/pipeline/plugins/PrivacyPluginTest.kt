package org.migor.feedless.pipeline.plugins

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.migor.feedless.common.HttpService

internal class PrivacyPluginTest {

  @Test
  @Disabled
  fun inlineImages() {
    val plugin = PrivacyPlugin()
    plugin.httpService = HttpService()
    val d = Jsoup.parse(
      """<html><body><div class="article-layout__header-container">
  <header class="a-article-header">
    <h1 class="a-article-header__title">
      Missing Link: Was es mit der radikalen Theorie zur Dunklen Energie auf sich hat
    </h1>
    <figure>
      <img
        src="https://heise.cloudimg.io/v7/_www-heise-de_/imgs/18/4/1/5/4/5/6/9/noirlab2310a-396394ddb135ff22.jpeg?force_format=avif%2Cwebp%2Cjpeg&amp;org_if_sml=1&amp;q=85&amp;width=610"
        width="1643" height="924" alt="" style="aspect-ratio: 1643 / 924; object-fit: cover;">
      <figcaption class="a-caption "><p class="a-caption__text">

        Das Schwarze Loch im Zentrum der elliptischen Riesengalaxie Messier 87 in einer neuen KI-unterstützten
        Bearbeitung von L. Medeiros (Institute for Advanced Study), D. Psaltis (Georgia Tech), T. Lauer (NSF’s
        NOIRLab), and F. Ozel (Georgia Tech).

      </p>
        <p class="a-caption__source">
          (Bild:&nbsp;<a href="https://noirlab.edu/public/images/noirlab2310a/" target="_blank"
                         rel="external noopener">NORLab</a>, <a href="https://creativecommons.org/licenses/by/4.0/"
                                                                target="_blank" rel="external noopener">CC BY 4.0</a>)
        </p>
      </figcaption>
    </figure>
  </header>
</div></body></html>
""".trimIndent(),
      "https://www.heise.de/hintergrund/Missing-Link-Was-es-mit-der-radikalen-Theorie-zur-Dunklen-Energie-auf-sich-hat-8988403.html"
    )
    val markup = plugin.inlineImages("", d)
    val images = Jsoup.parse(markup).select("img[src]")
    Assertions.assertTrue(images.isNotEmpty())
    val src = images.first()!!.attr("src")
    Assertions.assertTrue(src.startsWith("data:image/"))
    Assertions.assertTrue(src.length > 50)
  }
}
