package edu.illinois.nchen.akkaFutures

import akka.jsr166y.ForkJoinPool
import java.util.Arrays
import akka.dispatch.{Await, Future, ExecutionContext}
import akka.util.Duration
import edu.illinois.nchen.base.SequentialAnalysisEngine
import com.google.common.base.Stopwatch

class AkkaFuturesAnalysisEngine extends SequentialAnalysisEngine {

  // NOTES - Current problems/limitations/inconveniences with the current way:
  // 1) I am not a Scala programmer so some of these might not be idiomatic
  // 2) The way the for-comprehension works will order things. This creates an implicit dependency for things that
  //    are not dependent. e.g., nyseData and nasdaqData can be completed independently but we have to wait for them in
  //    order specified in the of the for-comprehension
  override def doAnalysisParallel() = {

    implicit val executor = ExecutionContext.fromExecutorService(new ForkJoinPool())

    val nyseData = Future(loadNyseData())
    val nasdaqData = Future(loadNasdaqData())

    val mergedMarketData = for {
      nyse <- nyseData
      nasdaq <- nasdaqData
    } yield mergeMarketData(Arrays.asList(nyse, nasdaq))

    val modeledMarketData = for {
      m <- mergedMarketData
      normalizedData <- Future(normalizeData(m))
      analyzedData <- Future(analyzeData(normalizedData))
    } yield runModel(analyzedData)

    val modeledHistoricalData = for {
      fedHistoricalData <- Future(loadFedHistoricalData())
      normalizedHistoricalData <- Future(normalizeData(fedHistoricalData))
      analyzedHistoricalData <- Future(analyzeData(normalizedHistoricalData))
    } yield runModel(analyzedHistoricalData)

    val results = for {
      marketData <- modeledMarketData
      historicalData <- modeledHistoricalData
    } yield compareModels(Arrays.asList(marketData, historicalData))

    Await.result(results, Duration.Inf)
  }
}

object AkkaFuturesAnalysisEngine {
  def main(args: Array[String]) = {
    val engine = new AkkaFuturesAnalysisEngine()
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