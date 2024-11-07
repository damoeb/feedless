package org.migor.feedless.transform

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.scrape.PageInspectionService
import org.migor.feedless.scrape.WebToArticleTransformer
import org.migor.feedless.util.JsonUtil
import org.springframework.util.ResourceUtils
import java.nio.file.Files

internal class WebToArticleTransformerTest {

  private lateinit var extractor: WebToArticleTransformer

  @BeforeEach
  fun setUp() {
    extractor = WebToArticleTransformer(PageInspectionService())
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "derstandard_at, https://derstandard.at",
      "newyorker_com, https://www.newyorker.com",
      "spiegel_de, https://www.spiegel.de",
      "theatlantic_com, https://www.theatlantic.com",
      "diepresse_com, https://www.diepresse.com",
      "medium_com, https://www.medium.com",
      "wordpress_com, https://www.wordpress.com",
      "wikipedia_org, https://www.wikipedia.org"
    ]
  )
  fun `article can be extracted from markup`(name: String, url: String) = runTest {
    val actual = extractor.fromHtml(readFile("${name}.html"), url, false)
    val rawJson = readFile("${name}.json")
    val expected = JsonUtil.gson.fromJson(rawJson, Map::class.java)
//    assertThat(expected.contentHtml).isEqualTo(actual.contentHtml)
//    assertThat(expected.contentText).isEqualTo(actual.contentText)
//    assertThat(expected.publishedAt).isEqualTo(actual.publishedAt)
    assertThat(expected["url"]).isEqualTo(actual.url)
    assertThat(expected["title"]).isEqualTo(actual.title)
//    assertThat(expected.faviconUrl).isEqualTo(actual.faviconUrl)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "derstandard_at;;Räumung des Protestcamps gegen Stadtstraße für Ludwig nicht vom Tisch;;Sollten die Besetzer nicht gehen, wird das Camp laut Wiens Stadtchef Ludwig \"in letzter Konsequenz\" geräumt. Umweltorganisationen protestieren gegen die Anwaltsbriefe;;https://i.ds.at/iytDAA/rs:fill:1200:600/plain/2021/12/15/ludwig002.jpg;;de",
      "newyorker_com;;The Case Against Civilization;;Did our hunter-gatherer ancestors have it better?;;https://media.newyorker.com/photos/59b197320a52a80d35bc87dd/16:9/w_1280,c_limit/170918_r30523.jpg;;en",
      "spiegel_de;;(S+) Coronavirus: Nach Impfstoffinventur von Karl Lauterbach - Fehlen in Deutschland wirklich Impfstoffe?;;Der neue Bundesgesundheitsminister Karl Lauterbach beklagt Impfstoffmangel für die Boosterkampagne im kommenden Jahr. Experten sind von seiner Rechnung irritiert.;;https://cdn.prod.www.spiegel.de/images/d744a6cf-4ff3-49ee-af25-aa854dfe4646_w1280_r1.77_fpx66.54_fpy50.jpg;;de",
      "theatlantic_com;;Why America Can’t Test Like Europe;;America’s complicated health-care system means everything is harder—even rapid testing.;;https://cdn.theatlantic.com/thumbor/ZFer03leBhMV67aOVPWSnJNyU60=/0x53:2496x1353/960x500/media/img/mt/2021/12/GettyImages_1333335753/original.jpg;;en",
      "diepresse_com;;\"Herzliche Aufnahme\", heikle Agenda: Der Kanzler ist...;;Vor dem Beginn des EU-Treffens steckte Österreichs Kanzler Karl Nehammer Österreichs Positionen ab. Von der Frage der Schuldenunion bis Nord Stream 2. Beim Thema illegale Migration sieht er die...;;https://media.diepresse.com/social_diepresse_nachrichten/images/uploads_1200/2/1/5/6074901/C88BACF2-9A3C-4B28-B523-55C8268D242C_v0_l.jpg;;de",
      "medium_com;;Add version.properties file to your Android Project;;Change from one file and reflect the changes on all the product flavors;;https://miro.medium.com/max/800/1*NqH_DKh5JmPup66TrWdh-A.png;;en",
      "wordpress_com;;Knowns and Unknowns;;In a talk [0] by the battlesome philiosopher Slavoj Zizek, he mentioned a classification of knowledge, of which especially the Unkown-Knowns catched my interest. Known-Knowns: things we know that w…;;https://s0.wp.com/i/blank.jpg;;en",
      "wikipedia_org;;Barnum-Effekt – Wikipedia;;Der Barnum-Effekt ist ein Begriff aus der Psychologie. Er bezeichnet die Neigung von Menschen, vage und allgemeingültige Aussagen über die eigene P...;;;;de"
    ],
    delimiterString = ";;"
  )
  fun `summary can be extracted from markup`(
    name: String,
    title: String,
    text: String?,
    imageUrl: String?,
    language: String
  ) = runTest {
    val actual = extractor.fromHtml(readFile("${name}.html"), "https://foo.bar", true)
    assertThat(actual.title).isEqualTo(title)
    assertThat(actual.text).isEqualTo(text)
    assertThat(actual.imageUrl).isEqualTo(imageUrl)
    assertThat(actual.language).isEqualTo(language)
//    println(arrayOf(name, actual.title, actual.text, actual.imageUrl, actual.language).joinToString (";;"))
  }

  private fun readFile(ref: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-articles/$ref").toPath())
  }
}
