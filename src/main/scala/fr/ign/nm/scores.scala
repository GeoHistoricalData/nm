package fr.ign.nm

import better.files.File
import com.github.tototoshi.csv.CSVReader

/**
  */
trait scores {
  implicit def File2List(file: File) = CSVReader.open(file.toJava).all().map(l=>(l.head,l(1)))
  implicit def JavaFile2List(file: java.io.File) = CSVReader.open(file).all().map(l=>(l.head,l(1)))
  def countTruePositives(elements: List[(String,String)], prediction: List[(String,String)]) = {
    prediction.count(elements.contains)
  }
  def falsePositives(elements: List[(String,String)], prediction: List[(String,String)]) = {
    prediction.filterNot(elements.contains)
  }
  def falseNegatives(elements: List[(String,String)], prediction: List[(String,String)]) = {
    falsePositives(prediction, elements)
  }
  def compute(elements: List[(String,String)])(prediction: List[(String,String)]) = {
//    println(elements.size + " elements and " + prediction.size + " predictions")
    val true_positives = countTruePositives(elements, prediction)
//    println("true_positives " + true_positives + " out of " + prediction.size)
    val precision = true_positives.toDouble / prediction.size.toDouble
//    println("precision " + precision + " out of " + elements.size)
    val recall = true_positives.toDouble / elements.size.toDouble
//    println("recall " + recall)
    val f1Score = if (precision + recall == 0) 0.0 else 2 * precision * recall / (precision + recall)
//    println("f1Score " + f1Score)
    (precision, recall, f1Score)
  }
  def fscore(elements: List[(String,String)])(prediction: List[(String,String)]) = compute(elements)(prediction)._3
  def fscore(elementsFile: java.io.File)(prediction: List[(String,String)]): Double = fscore(JavaFile2List(elementsFile))(prediction)
  def compute(elementsFile: java.io.File)(prediction: List[(String,String)]): (Double, Double, Double) = compute(JavaFile2List(elementsFile))(prediction)
  def removeDoubles(elements: List[(String,String)]) = elements.foldLeft(List[(String,String)]())((l:List[(String,String)], matching:(String,String))=>if (l.contains(matching)) l else l:+matching)
}

object scores extends scores