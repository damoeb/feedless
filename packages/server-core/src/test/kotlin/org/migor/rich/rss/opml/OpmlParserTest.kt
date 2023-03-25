package org.migor.rich.rss.opml

import org.junit.jupiter.api.Test

import org.migor.rich.rss.service.OpmlService

internal class OpmlParserTest {

    @Test
    fun parse() {
      val parser = OpmlService()
      parser.parseOpml("""
<?xml version="1.0" encoding="UTF-8"?>

<opml version="1.0">
    <head>
        <title>Roberto subscriptions in feedly Cloud</title>
    </head>
    <body>
        <outline text="ML,BD,DA" title="ML,BD,DA">
            <outline type="rss" text="Tombone's Computer Vision Blog" title="Tombone's Computer Vision Blog" xmlUrl="http://quantombone.blogspot.com/feeds/posts/default" htmlUrl="http://www.computervisionblog.com/"/>
            <outline type="rss" text="Datascope Blog" title="Datascope Blog" xmlUrl="http://datascopeanalytics.com/rss/" htmlUrl="http://datascopeanalytics.com/rss/"/>
            <outline type="rss" text="Dr. Randal S. Olson" title="Dr. Randal S. Olson" xmlUrl="http://www.randalolson.com/feed/" htmlUrl="http://www.randalolson.com"/>
            <outline type="rss" text="Erin Shellman » Bot or Not" title="Erin Shellman » Bot or Not" xmlUrl="http://www.erinshellman.com/feed/" htmlUrl="http://www.erinshellman.com"/>
            <outline type="rss" text="Blog - Louis Dorard" title="Blog - Louis Dorard" xmlUrl="http://www.louisdorard.com/blog?format=rss" htmlUrl="http://www.louisdorard.com/blog/"/>
            <outline type="rss" text="DMR - Data Mining and Reporting - Blog" title="DMR - Data Mining and Reporting - Blog" xmlUrl="http://www.dataminingreporting.com/2/feed" htmlUrl="http://www.dataminingreporting.com/blog"/>
            <outline type="rss" text="The Data Incubator" title="The Data Incubator" xmlUrl="http://blog.thedataincubator.com/feed/" htmlUrl="http://blog.thedataincubator.com"/>
            <outline type="rss" text="Airbnb Engineering » Data" title="Airbnb Engineering » Data" xmlUrl="http://nerds.airbnb.com/data/feed/" htmlUrl="http://nerds.airbnb.com"/>
            <outline type="rss" text="All About Statistics" title="All About Statistics" xmlUrl="http://feeds.feedburner.com/statsblogs" htmlUrl="http://www.statsblogs.com"/>
        </outline>
    </body>
</opml>
      """.trimIndent())
    }
}
