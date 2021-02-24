package org.migor.rss.rich.score

import com.rometools.rome.feed.synd.SyndContent
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import org.migor.rss.rich.model.SourceEntry
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScoreService {

  fun score(sourceEntry: SourceEntry) {
    val props = Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    val pipeline = StanfordCoreNLP(props)

    val text = listOf(sourceEntry.content!!["title"], (sourceEntry.content!!["description"] as SyndContent).value)
      .joinToString(separator = "\n")
    val annotation = pipeline.process(text)
    val sentences = annotation.get(CoreAnnotations.SentencesAnnotation().javaClass)

    sentences.forEach { sentence ->
      run {
        val sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass().javaClass)
        System.out.println(sentiment + "\t" + sentence)
      }
    }
  }
}
