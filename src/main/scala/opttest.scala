import better.files.File
import com.github.tototoshi.csv.CSVWriter

/**
  * Created by julien on 29/05/17.
  */
object opttest extends App {
  val file1 = File("/home/julien/data/networkmatching/matching/snapshot_1825.0_1836.0_edges.shp")
  val file2 = File("/home/julien/data/networkmatching/matching/snapshot_1784.0_1791.0_edges.shp")
  /*
  val file1 = File("/home/julien/devel/nm/src/main/resources/jac.shp")
  val file2 = File("/home/julien/devel/nm/src/main/resources/ver.shp")
  val name1 = "NOM_ENTIER"
  val name2 = "NOM_ENTIER"
  val id1 = "gid"
  val id2 = "gid"
  */
  val name1 = None
  val name2 = None
  val id1 = "ID"
  val id2 = "ID"
  val a = 20.0
  val alpha = a
  val beta = 1.4
  val gamma = a
  val k = 6.0
  val selection = 1.0
  val matches = opt.buldMatches(file1, file2, name1, name2, id1, id2, a, alpha, beta, gamma, k)
  val writer = CSVWriter.open("opt.csv")
  matches.foreach {
    m =>
      println(m._1._3 + " => " + m._2._3)
      writer.writeRow(List(m._1._3, m._2._3))
  }
  writer.close
}
