package org.migor.rss.rich.score

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import org.migor.rss.rich.model.SourceEntry
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class ScoreService {

  private lateinit var pipeline: StanfordCoreNLP

  @PostConstruct
  fun onInit() {
    val props = Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    pipeline = StanfordCoreNLP(props)
  }

  fun score(sourceEntry: SourceEntry): Triple<Double, Double, Double> {
    val text = listOfNotNull(sourceEntry.title!!, sourceEntry.content)
      .joinToString(separator = "\n")
    val annotation = pipeline.process(text)
    val sentences = annotation.get(CoreAnnotations.SentencesAnnotation().javaClass)

    val groups = sentences
      .map { sentence -> sentence.get(SentimentCoreAnnotations.SentimentClass().javaClass) }
      .groupBy { s: String -> s }

    return Triple(fraction(groups["Positive"], sentences), fraction(groups["Neutral"], sentences), fraction(groups["Negative"], sentences))
  }

  private fun fraction(sentencesInGroup: List<String>?, all: List<Any>): Double {
    return if (sentencesInGroup == null) {
      0.0
    } else {
      sentencesInGroup.size / all.size.toDouble()
    }
  }
}
