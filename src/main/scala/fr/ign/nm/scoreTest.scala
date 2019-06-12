package fr.ign.nm

import better.files.File

object scoreTest extends App {
  //val elementFile = File("test_truth.csv")
  //val predictionFile = File("test_precision.csv")
  //val predictionFile = File("test_recall.csv")
  //val predictionFile = File("test_f1score.csv")
  val elementFile = File("truth.csv")
//  val predictionFile = File("opt_paris.csv")
  //val predictionFile = File("hmm_paris_lpsolve_50_resampling.csv")
  val elem = scores.File2List(elementFile)
//  val predictionFile1 = File("opt_north_east.csv")
//  val predictionFile2 = File("opt_north_west.csv")
//  val predictionFile3 = File("opt_south.csv")
//  val pred = scores.File2List(predictionFile)
//  val pred1 = scores.File2List(predictionFile1)
//  val pred2 = scores.File2List(predictionFile2)
//  val pred3 = scores.File2List(predictionFile3)
//  val predUnion = pred1 ++ pred2 ++ pred3
  val predUnion = scores.File2List(File("hmm_optim.csv"))
  //  val pred = predUnion.foldLeft(List[(String,String)]())((l:List[(String,String)], matching:(String,String))=>if (l.contains(matching)) l else l:+matching)
  val pred = scores.removeDoubles(predUnion)
  val results = scores.compute(elem)(pred)
  println("precision = " + results._1)
  println("recall    = " + results._2)
  println("f1score    = " + results._3)
//  println(scores.fscore(elem)(pred))

}
