package fr.ign.nm

import com.github.tototoshi.csv.{CSVReader, CSVWriter}

object MatchCompare extends App {
  val reader1 = CSVReader.open("manuel.csv")
  val reader2 = CSVReader.open("test_simplified.csv")
  val l1 = reader1.all().map(l=>(l(0).trim,l(1).trim))
  println("matches (auto): " + l1.size)
  val l2 = reader2.all().map(l=>(l(0).trim,l(1).trim))
  println("matches (manual): " + l2.size)
  scores.compute(l1)(l2)
  val falseP = scores.falsePositives(l1, l2)
  val falseN = scores.falseNegatives(l1, l2)
  println("false Positives: " + falseP.size)
  falseP.foreach(println)
  println("false Negatives: " + falseN.size)
  falseN.foreach(println)
}

//3526 elements and 3526 predictions
//true_positives 3520 out of 3526
//precision 0.998298355076574 out of 3526
//recall 0.998298355076574
//f1Score 0.998298355076574
//false Positives
//(3395,3196)
//(7517,197)
//(7475,1308)
//(3497,1312)
//(5539,877)
//(6459,1926)
//false Negatives
//(3395,3196)
//(7517,197)
//(7475,1308)
//(3497,1312)
//(5539,877)
//(6459,1926)
