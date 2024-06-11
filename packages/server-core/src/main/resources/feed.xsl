<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:atom="http://www.w3.org/2005/Atom"
        exclude-result-prefixes="atom"
>
    <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
                <title>Web Feed • <xsl:value-of select="atom:feed/atom:title"/></title>
                <style type="text/css">
                    body {
                        max-width: 90vw;
                        min-width:min(768px, 100vw);
                        margin:0 auto;
                        font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol";
                        font-size:16px;
                        line-height:1.5em
                    }
                    section {
                        margin:30px 15px
                    }
                    * {
                      height: auto!important;
                    }
                    h1 {
                        font-size:2em;
                        margin:.67em 0;
                        line-height:1.125em
                    }
                    h2 {
                        border-bottom:1px solid #eaecef;
                        padding-bottom:.3em
                    }
                    .alert {
                        background:#fff5b1;
                        padding:4px 12px;
                        margin:0 -12px
                    }
                    a {
                        text-decoration:none
                    }
                    img,
                    figure {
                      border: 1px solid black;
                      max-width: 80dvw;
                      max-height: 20dvh;
                      overflow: hidden;
                    }
                    svg {
                      max-width: 30px;
                      max-height: 30px;
                      overflow: hidden;
                    }
                    .entry h3 {
                        margin-bottom:0
                    }
                    .entry p {
                        margin:4px 0
                    }
                    body{
                        display: grid;
                        grid-template-areas: "alert alert alert" "info items items";
                    }

                    section:nth-of-type(1) {
                        grid-area: alert;
                    }

                    section:nth-of-type(2) {
                        grid-area: info;
                    }


                    section:nth-of-type(3) {
                        grid-area: items;
                    }

                </style>
            </head>
            <body>
                <section>
                    <div class="alert">
                        <p><strong>This is a web feed</strong>, also known as an RSS feed. <strong>Subscribe</strong> by copying the URL from the address bar into your newsreader app.</p>
                    </div>
                </section>
<!--                <section>-->
<!--                    <xsl:apply-templates select="atom:feed" />-->
<!--                </section>-->
                <section>
                  <h1><xsl:value-of select="atom:feed/atom:title"/></h1>
                  <p>This RSS feed provides the latest posts from "<xsl:value-of select="atom:feed/atom:title"/>".

                    <a class="head_link" target="_blank" href="https://github.com/voidfiles/awesome-rss">
                      What is an RSS Feed &#x2192;
                    </a>
                  </p>

                  <xsl:if test="atom:feed/atom:category">
                    <h2>Categories</h2>
                    <ul>
                      <xsl:for-each select="atom:feed/atom:category">
                        <li>
  <!--                        <a>-->
  <!--                          <xsl:attribute name="href">-->
  <!--                            <xsl:value-of select="concat(atom:feed/atom:link[@rel='self']/@href,'?tag=', @term)"  disable-output-escaping="yes" />-->
  <!--                          </xsl:attribute>-->

                            <xsl:value-of select="@term"/>
  <!--                        </a>-->
                        </li>
                      </xsl:for-each>
                    </ul>
                  </xsl:if>

                  <h2>Recent Items</h2>
                  <xsl:apply-templates select="atom:feed/atom:entry" />

                  <p>
<!--                    <xsl:if test="atom:feed/atom:link[@rel='previous'] != ''">-->
<!--                      <a>-->
<!--                        <xsl:attribute name="href">-->
<!--                          <xsl:value-of select="atom:feed/atom:link[@rel='previous']/@href"  disable-output-escaping="yes" />-->
<!--                        </xsl:attribute>-->
<!--                        Previous Page-->
<!--                      </a>-->
<!--                    </xsl:if>-->
<!--                    <a>-->
<!--                      <xsl:attribute name="href">-->
<!--                        <xsl:value-of select="atom:feed/atom:link[@rel='next']/@href"  disable-output-escaping="yes" />-->
<!--                      </xsl:attribute>-->
<!--                      Next Page-->
<!--                    </a>-->
                  </p>
                </section>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="atom:feed">
        <h1><xsl:value-of select="atom:title"/>'s Feed</h1>
        <p>This RSS feed provides the latest posts from <xsl:value-of select="atom:title"/>.

            <a class="head_link" target="_blank">
                <xsl:attribute name="href">
                    <xsl:value-of select="atom:link[@rel='alternate']/@href"/>
                </xsl:attribute>
                Visit Website &#x2192;
            </a>

        </p>

        <h2>What is an RSS feed?</h2>
        <p>An RSS feed is a data format that contains the latest content from a website, blog, or podcast. You can use feeds to <strong>subscribe</strong> to websites and get the <strong>latest content in one place</strong>.</p>
        <ul>
            <li><strong>Feeds put you in control.</strong> Unlike social media apps, there is no algorithm deciding what you see or read. You always get the latest content from the creators you care about.</li>
            <li><strong>Feed are private by design.</strong> No one owns web feeds, so no one is harvesting your personal information and profiting by selling it to advertisers.</li>
            <li><strong>Feeds are spam-proof.</strong> Had enough? Easy, just unsubscribe from the feed.</li>
        </ul>
        <p>All you need to do to get started is to add the URL (web address) for this feed to a special app called a newsreader.</p>
        <h2>Any RSS client / application recommendations?</h2>
        <p>Without explicit endorsement of a specific client, here are some open source applications you can use to read and aggregate RSS feeds from many sources:</p>
        <h3>Desktop</h3>
        <ul>
            <li><a href="https://www.thunderbird.net/en-US/">thunderbird</a>. desktop e-mail, calendar, and news (RSS + NNTP/Usenet) reader.</li>
            <li><a href="https://claws-mail.org/">Claws Mail</a>. desktop e-mail and news (NNTP) reader. Supports RSS with add-ons.</li>
            <li><a href="https://netnewswire.com/">NetNewsWire</a>. RSS reader for Mac, iPhone, and iPad.</li>
            <li><a href="https://flathub.org/apps/org.gabmus.gfeeds">Feeds</a>. desktop RSS reader for the GNOME desktop.</li>
        </ul>
        <h3>Mobile</h3>
        <ul>
            <li><a href="https://netnewswire.com/">NetNewsWire</a>. RSS reader for Mac, iPhone, and iPad.</li>
            <li><a href="https://f-droid.org/en/packages/com.nononsenseapps.feeder/">Feeder</a>. RSS reader for Android via F-Droid repository.</li>
            <li><a href="https://f-droid.org/packages/com.newsblur/">Newsblur</a>. Server that collects RSS/Atom feeds (there's also a freemium service at newsblur.com). Use a website or Android app to read.</li>
        </ul>
        <h3>Self-hosted</h3>
        <ul>
            <li><a href="https://f-droid.org/packages/com.newsblur/">Newsblur</a>. Server that collects RSS/Atom feeds (there's also a freemium service at newsblur.com). Use a website or Android app to read.</li>
            <li><a href="https://miniflux.app/">miniflux</a>. minimalistic self-hosted golang server, <a href="https://miniflux.app/hosting.html">offers paid hosting</a>.</li>
            <li><a href="https://github.com/nkanaev/yarr">yarr</a>. another minimalistic self-hosted golang server.</li>
        </ul>
    </xsl:template>

    <xsl:template match="atom:entry">
        <div class="entry">
            <h3>
                <a target="_blank">
                    <xsl:attribute name="href">
                        <xsl:value-of select="atom:link/@href"/>
                    </xsl:attribute>
                    <xsl:value-of select="atom:title"/>
                </a>
            </h3>
            <p>
              Link: <xsl:value-of select="atom:link/@href"/>
            </p>
            <p>
              Tags: <xsl:for-each select="atom:category">
                #<xsl:value-of select="@term"/>
              </xsl:for-each>
            </p>
            <p>
                <xsl:value-of select="atom:summary" disable-output-escaping="yes" />
            </p>
            <p>
              <xsl:if
                test="atom:content[starts-with(@type,'image/')]">
                <xsl:value-of select="concat('&lt;img src=data:image/png;base64,', atom:content[starts-with(@type,'image/')], '&gt;')"  disable-output-escaping="yes" />
              </xsl:if>
<!--              <xsl:if-->
<!--                test="atom:content[starts-with(@type,'html')]">-->
<!--                <xsl:value-of select="atom:content[starts-with(@type,'html')]" disable-output-escaping="yes" />-->
<!--              </xsl:if>-->
<!--              <xsl:if-->
<!--                test="atom:content[starts-with(@type,'text')]">-->
<!--                <xsl:value-of select="atom:content[starts-with(@type,'text')]" disable-output-escaping="yes" />-->
<!--              </xsl:if>-->
            </p>
            <small>
                Published: <xsl:value-of select="atom:updated" />
            </small>
        </div>
    </xsl:template>

</xsl:stylesheet>
