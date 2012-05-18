package edu.illinois.nchen.twitterFutures

import edu.illinois.nchen.base.SequentialAnalysisEngine
import com.google.common.base.Stopwatch
import java.util.Arrays
import akka.jsr166y.ForkJoinPool
import com.twitter.util.FuturePool


class TwitterFuturesAnalysisEngine extends SequentialAnalysisEngine {
  val executor = FuturePool(new ForkJoinPool())

  // NOTES - Current problems/limitations/inconveniences with the current way:
  // 1) There is very little documentation for how to use Futures asynchronously so what I am doing with FuturePool
  //   might be wrong
  override def doAnalysisParallel() = {
    val nyseData = executor(loadNyseData())
    val nasdaqData = executor(loadNasdaqData())

    val mergedMarketData = for {
      nyse <- nyseData
      nasdaq <- nasdaqData
    } yield mergeMarketData(Arrays.asList(nyse, nasdaq))

    val modeledMarketData = for {
      m <- mergedMarketData
      normalizedData <- executor(normalizeData(m))
      analyzedData <- executor(analyzeData(normalizedData))
    } yield runModel(analyzedData)

    val modeledHistoricalData = for {
      fedHistoricalData <- executor(loadFedHistoricalData())
      normalizedHistoricalData <- executor(normalizeData(fedHistoricalData))
      analyzedHistoricalData <- executor(analyzeData(normalizedHistoricalData))
    } yield runModel(analyzedHistoricalData)

    val results = for {
      marketData <- modeledMarketData
      historicalData <- modeledHistoricalData
    } yield compareModels(Arrays.asList(marketData, historicalData))

    results.get()
  }
}

object TwitterFuturesAnalysisEngine {
  def main(args: Array[String]) = {
    val engine = new TwitterFuturesAnalysisEngine()
    var watch: Stopwatch = null

    println("==SEQUENTIAL==")
    watch = new Stopwatch().start()
    engine.doAnalysisSequential()
    watch.stop()
    println(watch.elapsedMillis() + "ms taken.")

    System.out.println("==PARALLEL==")
    watch = new Stopwatch().start()
    engine.doAnalysisParallel()
    watch.stop()
    println(watch.elapsedMillis() + "ms taken.")
  }
}