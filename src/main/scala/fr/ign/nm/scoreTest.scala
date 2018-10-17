package fr.ign.nm

import better.files.File

object scoreTest extends App {
  //val elementFile = File("test_truth.csv")
  //val predictionFile = File("test_precision.csv")
  //val predictionFile = File("test_recall.csv")
  //val predictionFile = File("test_f1score.csv")
  val elementFile = File("manuel.csv")
  val predictionFile = File("opt_paris.csv")
  //val predictionFile = File("hmm_paris_lpsolve_50_resampling.csv")
  val elem = scores.File2List(elementFile)
  val pred = scores.File2List(predictionFile)
  val results = scores.compute(elem)(pred)
  println("precision = " + results._1)
  println("recall    = " + results._2)
  println("f1score    = " + results._3)
  println(scores.fscore(elem)(pred))

}
