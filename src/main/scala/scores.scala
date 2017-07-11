import better.files.File
import com.github.tototoshi.csv.CSVReader

/**
  */
object scores extends App {
  def countTruePositives(elements: List[(String,String)], prediction: List[(String,String)]) = {
    prediction.count(elements.contains)
  }
  def scores(elements: List[(String,String)], prediction: List[(String,String)]) = {
    val truepositives = countTruePositives(elements, prediction)
    val precision = truepositives.toDouble / prediction.size
    val recall = truepositives.toDouble / elements.size
    val f1Score = 2 * precision * recall / (precision + recall)
    (precision, recall, f1Score)
  }
  def compute(elementFile: File, predictionFile: File) = {
    val elementReader = CSVReader.open(elementFile.toJava)
    val predictionReader = CSVReader.open(predictionFile.toJava)
    val s = scores(elementReader.all().map(l=>(l(0),l(1))), predictionReader.all().map(l=>(l(0),l(1))))
    println("precision = " + s._1)
    println("recall    = " + s._2)
    println("f1score    = " + s._3)
  }
  val elementFile = File("test_truth.csv")
  //val predictionFile = File("test_precision.csv")
  //val predictionFile = File("test_recall.csv")
  val predictionFile = File("test_f1score.csv")
  compute(elementFile, predictionFile)
}
