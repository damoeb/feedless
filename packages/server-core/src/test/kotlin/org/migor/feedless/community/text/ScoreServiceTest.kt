package org.migor.feedless.community.text

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import io.jenetics.DoubleChromosome
import io.jenetics.DoubleGene
import io.jenetics.Genotype
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.migor.feedless.community.ScoreService
import org.migor.feedless.community.ScoreWeights
import org.migor.feedless.community.text.complex.CivilityWeights
import org.migor.feedless.community.text.complex.OriginalityWeights
import org.migor.feedless.community.text.complex.QualityWeights
import org.migor.feedless.community.text.complex.RelevanceScorer
import org.migor.feedless.community.text.complex.RelevanceWeights
import org.migor.feedless.community.text.simple.EngagementScorer
import org.migor.feedless.community.text.simple.SpellingScorer
import org.migor.feedless.document.any
import org.migor.feedless.license.LicenseService
import org.migor.feedless.plan.ProductService
import org.migor.feedless.secrets.UserSecretService
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.math.pow


@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.community])
@MockBeans(
  value = [
    MockBean(UserSecretService::class),
    MockBean(LicenseService::class),
    MockBean(ProductService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
)
@Tag("nlp")
class ScoreServiceTest {

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var engagementScorer: EngagementScorer

  @Autowired
  lateinit var relevanceScorer: RelevanceScorer

  @MockBean
  lateinit var spellingScorer: SpellingScorer

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun score() {
    assertThat(scoreService).isNotNull
    val comment = mock(CommentEntity::class.java)
    Mockito.`when`(comment.contentText).thenReturn("Du erzählst nur scheisse")

    val neutral = 1.0
    val weights = ScoreWeights(
      civility = CivilityWeights(
        sentiment = neutral,
        attacks = neutral,
        politeness = neutral
      ),
      quality = QualityWeights(
        engagement = neutral,
        citation = neutral,
        vocabulary = neutral,
        spelling = neutral,
        wordCount = neutral,
        ease = neutral,
      ),
      relevance = RelevanceWeights(
        context = neutral,
      ),
      originality = OriginalityWeights(
        duplicate = neutral,
        novelty = neutral,
        spam = neutral,
        links = neutral,
      )
    )
    assertThat(scoreService.score(comment, weights)).isCloseTo(0.3, within(0.01))
  }

  @Test
  fun testEstimatePenalty() {
    val expected = listOf(1,2,3,4)
    assertThat(estimatePenalty(listOf(1,2,3,4), expected)).isEqualTo(0)
    assertThat(estimatePenalty(listOf(1,2,4,3), expected)).isEqualTo(-2)
  }


  @Test
  @Disabled
  fun train() {
    val comments = listOf(
      mapOf(
        0 to "When I was there (2005-2006, so, ancient history maybe), Google had a cultural problem with languages like Javascript. All their coding standards were developed for languages like C++ and Java and the typical Google engineer had the most experience in such languages. Projects that promised to relieve the programmer from having to write in nasty Javascript (such as GWT) had a lot of support. There are islands of Javascripters in Google, but many of them are working on the browser initiatives.\nThat said, the article is right about the importance of peer review at Google, although the headline is typical journalistic bait. Google's culture isn't so much engineering as it is science. Business is understood as an ongoing experiment. Opinions are worthless, only data counts.\nThe results are stunning and many cowboy developers have become converts. There are many parts of Google's codebase that are so beautiful they almost brought tears to my eyes. You don't know what it's like to see megabyte after megabyte of source code scroll by, and <i>you can't tell who wrote it</i>. The same conventions are used everywhere, \"tricky\" parts are at an absolute minimum, and good documentation is usually a given.\nIf there's one thing about Google culture that is worth preserving in all the startups now budding off, it's the peer review.",
        1 to "> You don't know what it's like to see megabyte after megabyte of source code scroll by, and you can't tell who wrote it.\nI disagree with this statement.  I think having uniformly good code is, indeed, a good thing.  On the other hand, I don't believe that means you should take it so far that you can't tell who wrote the code.  Good developers can, in fact, differ about how to write good code.\nIn fact my major general purpose complaint about Google is that they'd take a good idea and then take it way too far.",
        2 to "Both this and the parent (and even the OP, controlling for Cringely) give a fascinating perspective on Google's culture of peer review. Just out of curiosity, can you give some examples of how coding standards developed for C++ and Java wouldn't translate well to JS? (I'm a little surprised to hear about an anti-JS bias there, actually, though it does explain GWT.)",
        3 to "Do you mind if I ask which part of Google you worked with? I mean, particular products? Or are you still NDA'd? :S",
        4 to "> You don't know what it's like to see megabyte after megabyte of source code scroll by, and you can't tell who wrote it.\nSounds like they have an inefficient meat-machine based code generator.  Why not just use their considerable resources and automate that process?"
      ),
      mapOf(
        0 to "I think they could have worded this better:\n" +
          "\"GitHub offers the largest free storage quota among the big SCM hosters, and we came to the conclusion that we didn’t want to subsidize that quota for non-Ruby developers.\"<p>It sounds odd Engine Yard hosts an entire for-profit company for free just in exchange for some complimentary accounts.<p>My imagination tells me GitHub is tired of being slow and Engine Yard couldn't do anything more to help them.  Instead of the news being \"GitHub Leaves Engine Yard Because It's Too Slow\" the news is \"Engine Yard Kicks GitHub To The Curb Because GitHub has Non-Ruby Repos.\"<p>I don't see either headline being positive towards Engine Yard.",
        1 to """Ok, as far as I can tell, the real story is that Engine Yard relies on a GFS/SAN setup that doesn't scale in the unique way that Github needs.<p>If you think about it, Github is one of the few sites that actually directly uses the filesystem heavily.  Everyone else hits scaling issues on the DB first.<p><pre><code>  The sad thing of all of this is it's not really a matter
  of scaling, and it never has been. Our bottleneck has
  always been the file system. GFS just... sucks. I'm sorry,
  but I have to say it. Case in point, your graph. The first
  rebuild I ran timed out because of GFS. The second one ran
  fine, took maybe a minute to process, if that. GFS impacts
  everything... gem build failures due to cloning... GFS.
  Network graphs taking long time to build... GFS. Caching
  jobs not completing... GFS. I think you see where I'm
  going here. There's no plans to deploy the new code to the
  live servers, and I think the reason is that we're afraid
  it'll make GFS performance worse, not better. But on the
  new servers where we don't have to fight GFS, it's
  amazing.</code></pre>""",
        2 to """Well, EY could make Github fast again by providing more resources.  But then, someone would have to pay for the additional resources.  EY doesn't think it's worth it, and Github can get the additional resources elsewhere for cheaper (EY is expensive, because you pay for premium support).<p>It seems like it's more about the fit between the companies, than any particular performance issues.""",
        3 to """It's also inaccurate: GitHub doesn't offer the largest free storage quota. That's still Google Code, which offers 1024 mb/project, rather than 300 mb/user.""",
        4 to "&#62; It sounds odd Engine Yard hosts an entire for-profit company for free just in exchange for some complimentary accounts.<p>why?  its fairly high quality, targeted advertising with a built-in demonstration.",
        5 to """&#62; It sounds odd Engine Yard hosts an entire for-profit company for free just in exchange for some complimentary accounts.<p>Well, sort of.  It's really a value trade-off.  EY offers its customers something that would cost them ${'$'}50 a month, so it's costing Github (customers) * ${'$'}50 to offer that.  They exchange the lost value for added value in hosting (i.e., they're "losing" a few thousand a month in free accounts for our customers, so we'll offer them free hosting to continue to offer that).<p>It's simple barter.""",
        6 to "It's like when your boss let's you \"resign\" so you don't have to tell a future employer you were fired. EY was fired.",
      ),
      mapOf(
        0 to "Southern senators need their pork. Logically the south is not the best place to put rail because they do not have the large population centers, but politically there has to be just as much rail in the south as in the northeast or the southern congresspeople will destroy any legislation.<p>This is one of the reasons Amtrak has been losing money, btw.",
        1 to "The other reasons being that they provide a mostly terrible service that is impractical for routine use to an uninteresting market with an ineffective pricing scheme. But yeah, pork is bad too.",
        2 to "This statement confuses me. Looking at that map, it looks like most of the rail is being put in places that could most use it. There are a few confusing places (like the Buffalo-Cleveland being left out), but for the most part it seems to be linking the major city centers for those areas together. I do wonder about that little Florida circuit, especially why it isn't being connected to the rest of the huge Houston-&#62;All of the East coast circuit. I also wonder why Kentucky/Tennessee are left out.",
        3 to "\"Amtrak operates corridor routes (covering distances under 400 miles) and long-distance routes (over 400 miles in length)...Virtually all of Amtrak's 44 or so routes lose money but the long-distance routes lose the most...\n" +
          "In congressional testimony the DOT IG stated that long-distance trains accounted for only 15% of total inter-city ridership..\"<p><a href=\"http://lieberman.senate.gov/documents/crs/amtrak.pdf\" rel=\"nofollow\">http://lieberman.senate.gov/documents/crs/amtrak.pdf</a>",
        4 to "The South does have large population centers, it's just that they're rather far apart. The northeast, maybe extending into Chicago, is the one and only part of the US where a high-speed rail network would make sense, but it's silly to expect a government to do things just because they make sense.",
        5 to "Maybe the population in the South will increase as a result?",
      )
    )

    Mockito.`when`(spellingScorer.calculateErrorRate(anyString(), any(Locale::class.java))).thenReturn(0.0)

    val gtf: Genotype<DoubleGene> = Genotype.of(
      DoubleChromosome.of(
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
        DoubleGene.of(0.0, 1.0),
      ),
    )

    val engine = Engine.builder({ result -> fitness(result, comments) }, gtf).build()
    val result = engine.stream().limit(10).collect(EvolutionResult.toBestGenotype())
    println("Selected: " + result + ". Sum: " + fitness(result, comments, true).toString())

  }

  private fun fitness(result: Genotype<DoubleGene>, commentsGroups: List<Map<Int, String>>, log: Boolean = false): Int {
    val chromosomes = result.chromosome().stream().toList()

    var i = 0
    val inactive = 0.0
    val weights = ScoreWeights(
      civility = CivilityWeights(
        sentiment = inactive,
        attacks = inactive,
        politeness = inactive
      ),
      quality = QualityWeights(
        engagement = inactive,
        citation = chromosomes[i++].allele(),
        vocabulary = chromosomes[i++].allele(),
        spelling = inactive,
        wordCount = chromosomes[i++].allele(),
        ease = chromosomes[i++].allele(),
      ),
      relevance = RelevanceWeights(
        context = chromosomes[i++].allele(),
      ),
      originality = OriginalityWeights(
        duplicate = chromosomes[i++].allele(),
        novelty = chromosomes[i++].allele(),
        spam = chromosomes[i++].allele(),
        links = chromosomes[i].allele(),
      )
    )

    var sum = 0
    for (comments in commentsGroups) {
      val parent = mock(CommentEntity::class.java)
      Mockito.`when`(parent.contentText).thenReturn(patchCommentValue(comments[0]!!))
      val commentGraphService = mock(CommentGraphService::class.java)
      Mockito.`when`(commentGraphService.getParent(any(CommentEntity::class.java))).thenReturn(parent)
      Mockito.`when`(commentGraphService.getReplyCount(any(CommentEntity::class.java))).thenReturn(0)
      engagementScorer.commentGraphService = commentGraphService
      relevanceScorer.commentGraphService = commentGraphService

      sum += score(comments, weights, log)
    }

    println("$sum")

    return sum
  }

  private fun patchCommentValue(v: String): String {
    return v.replace("<p>", "\n").replace("&#62;", ">")
  }

  private fun score(
    comments: Map<Int, String>,
    weights: ScoreWeights,
    log: Boolean
  ): Int {
    val order = comments.filterKeys { it != 0 }
      .map {
        val comment = mock(CommentEntity::class.java)
        Mockito.`when`(comment.contentText).thenReturn(it.value)

        scoreService.score(comment, weights)
      }

    return estimatePenalty(order, order.sorted(), log)
  }

  private fun <T> estimatePenalty(actual: List<T>, expected: List<T>, log: Boolean = false): Int {
    return expected.mapIndexed { index, score ->
      run {
        val penalty = (actual.indexOf(score) - index).toDouble().pow(2.0).toInt()
        if (log) {
          println("actual $index expected ${actual.indexOf(score)} -> $penalty")
        }
        -penalty
      }
    }.sum()
  }
}
