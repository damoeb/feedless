package org.migor.feedless.feed.exporter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.harvest.HarvestResponse

val rawFeed = """
      <?xml version="1.0" encoding="utf-8"?>
      <?xml-stylesheet type="text/xsl" media="screen" href="/static/podcast/podcast.xsl"?>
      <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"
           xmlns:media="http://search.yahoo.com/mrss/"
           xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
        <channel> <!--/esi/podcast-xml-podcast-summary.xml?drsearch%3Aprops=%7B%22__typename%22%3A%22Page%22%2C%22pageType%22%3A%22page-podcast-only%22%2C%22sophoraId%22%3A%22kakadu-104%22%2C%22sophoraExternalId%22%3A%22PAPAYA_PODCAST_2730_253%22%2C%22contentType%22%3Anull%2C%22siteOrigin%22%3A%22deutschlandfunk-kultur%22%7D-->
          <title>Kakadu – Der Kinderpodcast - Deutschlandfunk Kultur</title>
       <link>https://www.kakadu.de/kakadu-104.html</link>
       <atom:link rel="self" type="application/rss+xml" href="https://www.kakadu.de/kakadu-104.xml" />
       <description>Kakadu ist der Kinderpodcast von Deutschlandfunk Kultur: Bunt, frech, fröhlich und schlau. Wir entdecken gemeinsam die Welt und beantworten die Fragen, die neugierige Kinder stellen! </description>
       <category>Info</category>
       <copyright>Deutschlandradio - deutschlandradio.de</copyright>
       <ttl>60</ttl>
       <language>de-DE</language>
       <pubDate>Tue, 18 Apr 2023 14:11:13 +0200</pubDate>
       <lastBuildDate>Tue, 18 Apr 2023 14:11:13 +0200</lastBuildDate>
       <image>
         <url>https://assets.deutschlandfunk.de/FILE_869f7ed9b25625bceb9bfe2d15fe6f96/1920x1920.jpg?t=1603448094558</url>
         <title>Kakadu – Der Kinderpodcast</title>
         <link>https://www.kakadu.de/kakadu-104.html</link>
         <description>Kakadu ist der Kinderpodcast von Deutschlandfunk Kultur: Bunt, frech, fröhlich und schlau. Wir entdecken gemeinsam die Welt und beantworten die Fragen, die neugierige Kinder stellen! </description>
       </image>
       <itunes:subtitle>Die Beiträge zur Sendung</itunes:subtitle>
       <itunes:new-feed-url>https://www.kakadu.de/kakadu-104.xml</itunes:new-feed-url>
       <itunes:image href="https://assets.deutschlandfunk.de/FILE_869f7ed9b25625bceb9bfe2d15fe6f96/1920x1920.jpg?t=1603448094558" />
       <itunes:owner>
         <itunes:name>Redaktion deutschlandradio.de</itunes:name>
         <itunes:email>podcast@deutschlandradio.de</itunes:email>
       </itunes:owner>
       <itunes:author>Deutschlandfunk Kultur</itunes:author>
       <itunes:explicit>No</itunes:explicit>
       <itunes:category text="Kids &amp; Family" /> <!--/esi/podcast-xml-podcast-feed.xml?drsearch%3Aprops=%7B%22itemReference%22%3A%7B%22__typename%22%3A%22SophoraReference%22%2C%22sophoraReferenceId%22%3A%22dira_DRK_91ab3541%22%2C%22primaryType%22%3A%22dradio-nt%3Apage-audio%22%7D%2C%22isTitleFirst%22%3Afalse%2C%22sitePodcastUrl%22%3A%22www.kakadu.de%22%2C%22sortBy%22%3A%22publicationDate%22%2C%22refSophoraId%22%3A%22kakadu-104%22%7D-->
          <item>
        <title>Ab zur Dentalpraktikerin - Was machen Tiere bei Zahnschmerzen?</title>
        <link>https://www.kakadu.de/was-machen-tiere-bei-zahnschmerzen-100.html</link>
        <description>
          <![CDATA[Spätestens wenn das leckere Heu unzerkaut wieder ausgespuckt wird, sollte man mal nachsehen: Hat das Pferd vielleicht Zahnweh? Und was dann? Es kann ja schlecht in die Zahnarztpraxis gehen. Stimmt! Deswegen kommt die Praxis zum Pferd! mit Thandi und Ryke]]>
        </description>
        <content:encoded>
          <![CDATA[<img src="https://assets.deutschlandfunk.de/7242adc0-4fd1-4eda-9ce1-c50b87ab15a9/2400x1350.jpg?t=1684232503853" alt="Eine Person von hinten, eine Stirnlampe tragend. Sie schiebt die Lippen eines Pferdes mit der Hand auseinander und behandelt die Zähne des Tieres mit einem dentalmedizinischen Gerät." title="Eine Person von hinten, eine Stirnlampe tragend. Sie schiebt die Lippen eines Pferdes mit der Hand auseinander und behandelt die Zähne des Tieres mit einem dentalmedizinischen Gerät."
                          width="144" height="81" border="0" align="left" hspace="4" vspace="4"/>Spätestens wenn das leckere Heu unzerkaut wieder ausgespuckt wird, sollte man mal nachsehen: Hat das Pferd vielleicht Zahnweh? Und was dann? Es kann ja schlecht in die Zahnarztpraxis gehen. Stimmt! Deswegen kommt die Praxis zum Pferd!<br clear="all"/><br/>mit Thandi und Ryke<br/><a title="Direkter Link zur Audiodatei" href="https://podcast-mp3.dradio.de/podcast/2023/05/16/kakadu_podcast_was_machen_tiere_bei_zahnschmerzen_drk_20230516_1400_91ab3541.mp3?refId=kakadu-104">Direkter Link zur Audiodatei</a><br/><p><br/></p>]]>
        </content:encoded>
        <media:content url="https://0.gravatar.com/avatar/foo?d=identicon&#38;r=G" medium="image">
          <media:title type="html">foo</media:title>
        </media:content>
        <guid>https://podcast-mp3.dradio.de/podcast/2023/05/16/kakadu_podcast_was_machen_tiere_bei_zahnschmerzen_drk_20230516_1400_91ab3541.mp3?refId=kakadu-104</guid>
        <pubDate>Tue, 16 May 2023 14:00:00 +0200</pubDate>
        <enclosure url="https://podcast-mp3.dradio.de/podcast/2023/05/16/kakadu_podcast_was_machen_tiere_bei_zahnschmerzen_drk_20230516_1400_91ab3541.mp3?refId=kakadu-104" length="24172021" type="audio/mpeg" />
        <itunes:author>Johanna Fricke</itunes:author>
        <itunes:duration>25:07</itunes:duration>
      </item>
      </channel>
      </rss>
    """.trimIndent()

class SyndAtomFeedExporterTest {

  @Test
  fun toAtom() {
    val corrId = ""
    val url = "https://foo.bar"
    val response = HttpResponse(
      contentType = "application/xml",
      url = url,
      statusCode = 200,
      responseBody = rawFeed.toByteArray(),
    )
    val feed = XmlFeedParser().process(corrId, HarvestResponse(url, response))
    val exporter = SyndAtomFeedExporter()
    val atom = exporter.toAtom(corrId, feed)
//    assertThat(atom).contains("Johanna Fricke")
    assertThat(atom).contains("Ab zur Dentalpraktikerin - Was machen Tiere bei Zahnschmerzen?")
  }
}
