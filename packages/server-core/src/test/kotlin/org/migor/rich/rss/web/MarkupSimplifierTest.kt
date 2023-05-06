package org.migor.rich.rss.web

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.rich.rss.util.JsonUtil
import org.springframework.util.ResourceUtils
import java.nio.file.Files

@Disabled
// todo fix
internal class MarkupSimplifierTest {

  private lateinit var simplifier: MarkupSimplifier

  @BeforeEach
  fun up() {
    simplifier = MarkupSimplifier()
  }

  @Test
  fun replacesCustomTags() {
    val actual = simplifier.simplify(parse("<app-article>lorem ipsum <b>this</b> or <EM>that</em></app-article>"))
    assertEquals("<article>lorem ipsum <b>this</b> or <em>that</em></article>", actual)
  }

  @Test
  fun allowsSemanticTags() {
    val actual =
      simplifier.simplify(parse("<article><header>title</header><section>lorem ipsum</section> <a href=\"http://google.de\">or</a> <p>that</p><footer>the foot</footer></article>"))
    assertEquals(
      """<article><header> title </header> <section> lorem ipsum </section> <a href="http://google.de">or</a> <p>that</p> <footer> the foot </footer></article>""",
      actual
    )
  }

  @Test
  @Disabled
  fun allowsImages() {
    val actual = simplifier.simplify(
      parse(
        """
      <div gravityscore="0">
              <figure data-type="image" data-fullscreen-enabled="true"> <picture>
                <source class="swiper-lazy" media="(max-width: 959px)" srcset="https://i.ds.at/c3pwFg/rs:fill:750:0/plain/2021/12/15/ludwig002.jpg">
                <source class="swiper-lazy" media="(min-width: 1690px)" srcset="https://i.ds.at/SDFIrQ/rs:fill:265:0/plain/2021/12/15/ludwig002.jpg">
                <source class="swiper-lazy" media="(min-width: 960px)" srcset="https://i.ds.at/mDjZ1g/rs:fill:930:0/plain/2021/12/15/ludwig002.jpg">
                <img referrerpolicy="unsafe-url" src="https://i.ds.at/c3pwFg/rs:fill:750:0/plain/2021/12/15/ludwig002.jpg" data-fullscreen-src="https://i.ds.at/E0co_g/rs:fill:1600:0/plain/2021/12/15/ludwig002.jpg">
                <button class="figure-fullscreen-trigger js-fullscreen-trigger"></button>
              </picture>
                <figcaption>
                  <p gravityscore="0">Bürgermeister Michael Ludwig (SPÖ) verteidigte am Mittwoch das Vorgehen der Stadt gegen die Klimaaktivisten.</p>
                </figcaption>
                <footer>
                  Foto: APA/HERBERT NEUBAUER
                </footer>
              </figure>
              <img src="https://i.ds.at/c3pwFg/rs:fill:750:0/plain/2021/12/15/ludwig002.jpg">
            </div>
    """.trimIndent()
      )
    )
    assertEquals(
      "<article><div> <figure> <picture> <img src=\"https://i.ds.at/c3pwFg/rs:fill:750:0/plain/2021/12/15/ludwig002.jpg\"> </picture> <figcaption> <p>Bürgermeister Michael Ludwig (SPÖ) verteidigte am Mittwoch das Vorgehen der Stadt gegen die Klimaaktivisten.</p> </figcaption> <footer> Foto: APA/HERBERT NEUBAUER </footer> </figure> <img src=\"https://i.ds.at/c3pwFg/rs:fill:750:0/plain/2021/12/15/ludwig002.jpg\"> </div></article>",
      actual
    )
  }

  @Test
  fun removesEmptyContainers() {
    val actual =
      simplifier.simplify(parse("<article><header></header><section>lorem ipsum<em></em></section> or <p>that</p><footer>the foot</footer></article><div><div><div><div><div></div></div></div>"))
    assertEquals(
      "<article><section> lorem ipsum </section> or <p>that</p> <footer> the foot </footer></article>",
      actual
    )
  }

  @Test
  fun flattensStructure() {
    val actual = simplifier.simplify(parse("<div><div><div><div>Hase</div></div></div></div>"))
    assertEquals("<article><div> Hase </div></article>", actual)
  }

  @Test
  @Disabled
  fun complex() {
    val actual = simplifier.simplify(resolveRef("derstandard_at"))
    assertEquals(
      "<article><div> <div> In dieser Galerie: 2 Bilder </div> <div> <div> <figure> <picture> <img src=\"https://i.ds.at/bMJKmQ/rs:fill:750:0/plain/2021/12/15/Stadtstrasse002.jpg\"> </picture> <figcaption> <p>Baustellen der Stadtstraße sind seit mehr als drei Monaten besetzt. Laut Stadt Wien verhindern nur die Aktivisten die Weiterführung. Geplant ist die Fertigstellung bis 2025.</p> </figcaption> <footer> Foto: Imago Images / Skata </footer> </figure> </div> <div> <figure> <picture> <img src=\"https://i.ds.at/c3pwFg/rs:fill:750:0/plain/2021/12/15/ludwig002.jpg\"> </picture> <figcaption> <p>Bürgermeister Michael Ludwig (SPÖ) verteidigte am Mittwoch das Vorgehen der Stadt gegen die Klimaaktivisten.</p> </figcaption> <footer> Foto: APA/HERBERT NEUBAUER </footer> </figure> </div> </div> </div> <p>Der Konflikt um die Besetzung der Baustellen für die Wiener Stadtstraße spitzt sich weiter zu. Am Mittwoch verteidigte Bürgermeister Michael Ludwig (SPÖ) das Vorgehen der Stadt gegen die Klimaaktivisten: Wie berichtet erhielten auch Jugendliche sowie Unterstützer und Sympathisanten des Protestcamps Anwaltsbriefe, in denen mit Schadenersatzforderungen seitens der Stadt gedroht wurde. \"Ich habe keine Klagsdrohung gemacht, sondern das ist aufgrund des Umstandes geschehen, dass es eine widerrechtliche Besetzung auf öffentlichem Grund gibt\", sagte Ludwig. \"Die Alternative dazu wäre eine sofortige Räumung.\" Diese ist freilich weiter nicht vom Tisch: \"In letzter Konsequenz\", meinte Ludwig, werde es diese geben – sofern die Besetzerinnen und Besetzer keine Bewegung zeigen würden.</p> <p>In den Anwaltsbriefen habe es jedenfalls \"weder eine Androhung in Millionenhöhe noch sonst etwas\" gegeben, sagte der Stadtchef. Er sei weiterhin offen für Gespräche mit den Aktivisten, Termine könnten mit seinem Büro vereinbart werden. Allerdings sei er nur gesprächsbereit mit Personen, \"die auch gesprächswillig sind. \"Wenn man mir über die Medien ausrichtet, ich brauche nur ins Besetzer-Camp kommen, um zu sagen, dass die Stadtstraße nicht gebaut wird, dann sehe ich da wenig Gesprächsspielraum.\" Eine Aufforderung der Aktivistinnen und Aktivisten vom Mittwoch, in den folgenden 48 Stunden einen Dialog auf Augenhöhe mit den Besetzern zu starten und die Klagsdrohungen zurückzunehmen, lehnte Ludwig ab. \"Was nicht geht, ist, dem Wiener Bürgermeister ein Ultimatum zu stellen.\"</p> <h3>Unbeantwortete Anfragen, sagen die NGOs</h3> <p>Dass Ludwig zu Gesprächen bereit gewesen sei, hatten zuvor Aktivisten des Protestcamps sowie Umwelt-NGOs vehement verneint. Sie berichteten, dass die Stadt auf Gesprächsanfragen nie eingegangen sei. Auch eine Anfrage von Amnesty zu einem persönlichen Termin mit der Stadtregierung sei bislang nicht beantwortet worden – weder von Ludwig noch von Vizebürgermeister Christoph Wiederkehr von den Neos.</p> <p>Annemarie Schlack, Geschäftsführerin von Amnesty International in Österreich, sprach im Zuge der Anwaltsbriefe im Auftrag der Stadt Wien gegen Umweltaktivisten von einer Menschenrechtsverletzung. \"Was sich wie ein Skandal anhört, ist auch einer. Nicht nur ein zivilgesellschaftlicher, sondern ein menschenrechtlicher.\"</p> <p>Die Schreiben seien als Slapp-Klagen zu qualifizieren: Der Begriff steht im Englischen für \"strategic lawsuit against public participation\" und beschreibt einen Schlag ins Gesicht für Einzelpersonen, aber auch für zivilgesellschaftliches Engagement. Laut Schlack dienen solche Klagen beziehungsweise Klagsandrohungen dazu, Menschen und Organisationen einzuschüchtern. \"Es geht immer darum, kritische Stimmen zum Schweigen zu bringen und Personen, die sich kritisch zu etwas äußern, zum Verstummen zu bringen.\" So hatten etwa auch die Verkehrsexperten Barbara Laa und Ulrich Leth, die nach Eigenangaben nie beim Protestcamp waren, Anwaltsbriefe erhalten. \"Dass wir es einmal mit einem Fall zu tun haben, den die Stadt Wien zu verantworten hat, hätte ich mir nie vorstellen können\", sagte Schlack.</p> <h3>Rücknahme der Klagsforderungen gefordert</h3> <p>Von der Stadt verlangt Amnesty Aufklärung, wie die Kanzlei des ehemaligen SPÖ-Justizsprechers Hannes Jarolim an Privatadressen der angeschriebenen Personen gekommen ist. Zudem sagte Schlack: \"Wir erwarten eine Rücknahme der Klagsdrohungen und die schleunigste Rückkehr zu sachlichem Dialog.\"</p> <p>Eine Rücknahme der Klagen fordert auch Sophie Lampl, Direktorin für Kommunikation und Kampagnen bei Greenpeace. Was passiere, wenn Ludwig wie angekündigt nicht in den folgenden 48 Stunden darauf reagiere, \"werden wir dann sehen\", sagte Lampl. Auch eine öffentliche Entschuldigung sei notwendig.</p> <h3>Ludwig verurteilt Greenpeace-Aktionen</h3> <p>Stadtchef Ludwig kritisierte hingegen das Vorgehen von Greenpeace in den letzten Wochen: Aktivisten hätten bei Gemeinderatssitzungen Räume besetzt und auch die Parteizentrale der SPÖ \"gewaltsam betreten\".</p> <h3>Greenpeace-Kritik auch an Neos</h3> <p>Lampl nahm in der Frage der möglichen Klagsdrohungen durch die Stadt auch den pinken Koalitionspartner in Wien in die Pflicht. \"Die Neos haben es noch immer nicht geschafft, Stellung zu nehmen und sich klar von dieser Vorgehensweise zu distanzieren. Das ist eine Schande für eine Partei, die für Bürgerrechte eintreten will.\"</p> <p>Auf STANDARD-Anfrage sagte eine Sprecherin von Vizebürgermeister Wiederkehr, dass das Vorgehen der SPÖ nicht mit den Neos akkordiert worden sei. Wiederkehr würde sich Gesprächen mit den Aktivisten nicht verschließen. Allerdings habe es noch keine diesbezügliche Anfrage gegeben. (Lara Hagen, David Krutzler, 15.12.2021)</p></article>",
      actual
    )
  }

  private fun resolveRef(ref: String): Element? {
    val expected = JsonUtil.gson.fromJson(readFile("${ref}.json"), ExtractedArticle::class.java)
    return parse(expected.content!!)
  }

  private fun parse(markup: String): Element? {
    return Jsoup.parse(markup).select("body").first()
  }

  private fun readFile(ref: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-articles/$ref").toPath())
  }
}
