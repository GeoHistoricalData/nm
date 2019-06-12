package fr.ign.nm

import better.files.File
import com.github.tototoshi.csv._
import org.geotools.data.shapefile.ShapefileDataStore
import org.locationtech.jts.geom.{Coordinate, Envelope, GeometryFactory, LineString, MultiLineString}
import org.locationtech.jts.index.strtree.{ItemBoundable, ItemDistance, STRtree}

import scala.util.Try

/**
  * Created by julien on 05/07/17.
  */
object matchloader extends App {
  val file1 = File("../HMMSpatialNetworkMatcher/manual_matching/snapshot_1784.0_1791.0_edges.shp")
  val file2 = File("../HMMSpatialNetworkMatcher/manual_matching/snapshot_1825.0_1836.0_edges.shp")
  val tolerance = 0.1
  val matches = File("../HMMSpatialNetworkMatcher/manual_matching/matching.shp")
  val result = "truth.csv"
  val revertGeometries = true
//  val result = "manuel.csv"
//  val matches = File("../HMMSpatialNetworkMatcher/test_simplified.shp")
//  val result = "test_simplified.csv"
//  val matches = File("./test_simplified.shp")
//  val result = "simplified.csv"
//  val matches = File("/home/julien/devel/morphogenesis-svn/results/result_paris_lpsolve_25_resampling.shp")
  def readShapefile(aFile: File, nameAttribute: Option[String], idAttribute: Option[String]) = {
    val store = new ShapefileDataStore(aFile.toJava.toURI.toURL)
    try {
      val reader = store.getFeatureReader
      var result = scala.collection.mutable.ArrayBuffer[(MultiLineString, Option[String], Option[String])]()
      while (reader.hasNext) {
        val feature = reader.next()
        val g = feature.getDefaultGeometry
        val geom = g match {
          case mls: MultiLineString => mls
          case _ => factory.createMultiLineString(Array(g.asInstanceOf[LineString]))
        }
        val name = if (nameAttribute.isDefined) Some(feature.getAttribute(nameAttribute.get).toString) else None
        val id = if (idAttribute.isDefined) Some(feature.getAttribute(idAttribute.get).toString) else None
        result.append((geom, name, id))
      }
      println(result.size)
      reader.close()
      Try{result.toArray}
//      try {
//        Try {
//          val featureReader = Iterator.continually(reader.next).takeWhile(_ => reader.hasNext)
//          val result = featureReader.map { feature =>
//            //println("feature")
//            println(feature.getAttribute(idAttribute.get))
//            val g = feature.getDefaultGeometry
//            val geom = if (g.isInstanceOf[MultiLineString]) g.asInstanceOf[MultiLineString] else factory.createMultiLineString(Array(g.asInstanceOf[LineString]))
//            val name = if (nameAttribute.isDefined) Some(feature.getAttribute(nameAttribute.get).toString) else None
//            val id = if (idAttribute.isDefined) Some(feature.getAttribute(idAttribute.get).toString) else None
//            println(id)
//            (geom, name, id)
//          }.toArray
//          println(result.size)
//          result
//        }
//      } finally reader.close
    } finally store.dispose()
  }
  def createIndex(seq: Array[(MultiLineString, Option[String], Option[String])]) = {
    val index = new STRtree()
    seq.foreach(f=> {
      val envelope = f._1.getEnvelopeInternal
      envelope.expandBy(tolerance)
//      println(factory.toGeometry(envelope))
      index.insert(envelope, f)
    })
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
//  val writer = CSVWriter.open("hmm_paris_lpsolve_50_resampling.csv")
  val writer = CSVWriter.open(result)
  def getMatches(c: Coordinate, index: STRtree):Array[(MultiLineString, Option[String], Option[String])] = {
    val point = factory.createPoint(c)
//    val envelope = point.buffer(tolerance).getEnvelopeInternal
    val envelope = new Envelope(c)
    envelope.expandBy(tolerance)
    val l = index.query(envelope).toArray.map(_.asInstanceOf[(MultiLineString, Option[String], Option[String])])
    l.filter(_._1.distance(point) <= tolerance)
  }
  seqMatches.foreach(m=>{
    val geom = m._1
    if (geom.getCoordinates.length != 2) println("Coordinates: " + geom.getCoordinates.length)
    val p1 = if (revertGeometries) m._1.getCoordinates.last else m._1.getCoordinates.head
    val l1 = getMatches(p1, index1)
    val p2 = if (revertGeometries) m._1.getCoordinates.head else m._1.getCoordinates.last
    val l2 = getMatches(p2, index2)
    if (l1.length!= 1) {
      println(l1.length + " points in db1 for " + factory.createPoint(p1).toText)
      val envelope = new Envelope(p1)
      envelope.expandBy(10.0*tolerance)
      println(factory.toGeometry(envelope))
      val l = index1.query(envelope).toArray.map(_.asInstanceOf[(MultiLineString, Option[String], Option[String])])
      println(l.length)
      l1.foreach {
        v=> println(v._3)
      }
    }
    if (l2.length!= 1) {
      println(l2.length+ " points in db2 for " + factory.createPoint(p2).toText)
      val envelope = new Envelope(p2)
      envelope.expandBy(tolerance * 10)
      val l = index2.query(envelope).toArray.map(_.asInstanceOf[(MultiLineString, Option[String], Option[String])])
      println(l.length)
      l2.foreach {
        v=> println(v._3)
      }
    }
    if ((l1.length!= 1) || (l2.length!= 1)) {
      val l1 = getMatches(p2, index1)
      val l2 = getMatches(p1, index2)
      if (l1.length!= 1) {
        println(l1.length+ " points in db1 for " + factory.createPoint(p2).toText + " after invert")
        l1.foreach {
          v=> println(v._3)
        }
      }
      if (l2.length != 1) {
        println(l2.length + " points in db2 for " + factory.createPoint(p1).toText + " after invert")
        l2.foreach {
          v=> println(v._3)
        }
      }
      if ((l1.length == 1) && (l2.length == 1)) {
        println("invert worked")
        val m1 = l1(0)
        val m2 = l2(0)
        //println("match " + m1._3 + " - " + m2._3)
        writer.writeRow(List(m1._3.get, m2._3.get))
        matchesCount = matchesCount + 1
      } else {
        println("FUCK")
      }
    } else {
      if ((l1.length == 1) && (l2.length == 1)) {
        val m1 = l1(0)
        val m2 = l2(0)
        //println("match " + m1._3 + " - " + m2._3)
        writer.writeRow(List(m1._3.get, m2._3.get))
        matchesCount = matchesCount + 1
      } else {
        println("FUCK IT")
      }
    }
  })
  writer.close
  println(matchesCount + " matches")
}
