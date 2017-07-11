import better.files.File
import com.vividsolutions.jts.geom.{GeometryFactory, MultiLineString}
import com.vividsolutions.jts.index.strtree.{ItemBoundable, ItemDistance, STRtree}
import org.geotools.data.shapefile.ShapefileDataStore
import com.github.tototoshi.csv._

import scala.util.Try

/**
  * Created by julien on 05/07/17.
  */
object matchloader extends App {
  val file1 = File("/home/julien/data/networkmatching/matching/snapshot_1825.0_1836.0_edges.shp")
  val file2 = File("/home/julien/data/networkmatching/matching/snapshot_1784.0_1791.0_edges.shp")
  val matches = File("/home/julien/data/networkmatching/matching/matching.shp")
  def readShapefile(aFile: File, nameAttribute: Option[String], idAttribute: Option[String]) = {
    val store = new ShapefileDataStore(aFile.toJava.toURI.toURL)
    try {
      val reader = store.getFeatureReader
      try {
        Try {
          val featureReader = Iterator.continually(reader.next).takeWhile(_ => reader.hasNext)
          val result = featureReader.map { feature =>
            println("feature")
            val geom = feature.getDefaultGeometry.asInstanceOf[MultiLineString]
            val name = if (nameAttribute.isDefined) Some(feature.getAttribute(nameAttribute.get).toString) else None
            val id = if (idAttribute.isDefined) Some(feature.getAttribute(idAttribute.get).toString) else None
            (geom, name, id)
          }.toArray
          result
        }
      } finally reader.close
    } finally store.dispose
  }
  def createIndex(seq: Array[(MultiLineString, Option[String], Option[String])]) = {
    val index = new STRtree()
    seq.foreach(f=>index.insert(f._1.getEnvelopeInternal, f))
    index
  }
  class MyItemDistance extends ItemDistance {
    def distance(item1: ItemBoundable, item2: ItemBoundable): Double = {
      val i1 = item1.getItem.asInstanceOf[(MultiLineString, Option[String], Option[String])]
      val i2 = item2.getItem.asInstanceOf[(MultiLineString, Option[String], Option[String])]
      i1._1.distance(i2._1)
    }
  }
  val distance = new MyItemDistance
  val factory = new GeometryFactory
  println("read shp 1")
  val seq1 = readShapefile(file1, None, Some("ID")).get
  var index1 = createIndex(seq1)
  println("read shp 2")
  val seq2 = readShapefile(file2, None, Some("ID")).get
  var index2 = createIndex(seq2)
  println("matches")
  val seqMatches = readShapefile(matches, None, None).get
  var matchesCount = 0
  val writer = CSVWriter.open("truth.csv")
  seqMatches.foreach(m=>{
    val p1 = m._1.getCoordinates()(0)
    val l1 = index1.query(factory.createPoint(p1).getEnvelopeInternal)
    val p2 = m._1.getCoordinates()(1)
    val l2 = index2.query(factory.createPoint(p2).getEnvelopeInternal)
    if (l1.size() != 1) println(l1.size + " points in db1 for " + factory.createPoint(p1).toText)
    if (l2.size() != 1) println(l2.size + " points in db2 for " + factory.createPoint(p2).toText)
    if ((l1.size == 1) && (l2.size == 1)) {
      val m1 = l1.get(0).asInstanceOf[(MultiLineString, Option[String], Option[String])]
      val m2 = l2.get(0).asInstanceOf[(MultiLineString, Option[String], Option[String])]
      println("match " + m1._3 + " - " + m2._3)
      writer.writeRow(List(m1._3.get, m2._3.get))
      matchesCount = matchesCount + 1
    }
  })
  writer.close
  println(matchesCount + " matches")
}
