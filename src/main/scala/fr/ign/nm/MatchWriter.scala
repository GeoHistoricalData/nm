package fr.ign.nm

import better.files.File
import org.geotools.data.{DataUtilities, DefaultTransaction}
import org.geotools.data.collection.ListFeatureCollection
import org.geotools.data.shapefile.{ShapefileDataStore, ShapefileDataStoreFactory}
import org.geotools.data.simple.SimpleFeatureStore
import org.geotools.feature.simple.SimpleFeatureBuilder
import org.locationtech.jts.geom.{GeometryFactory, LineString, MultiLineString}
import org.locationtech.jts.linearref.LengthIndexedLine

import scala.util.Try

/**
  * Created by julien on 05/07/17.
  */
object MatchWriter extends App {
  val workDirectory = File("../HMMSpatialNetworkMatcher/manual_matching")
  val file1 = workDirectory / "snapshot_1784.0_1791.0_edges.shp"
  val file2 = workDirectory / "snapshot_1825.0_1836.0_edges.shp"
  val out = File("matching_hmm_optim.shp")
  val truth = scores.File2List(File("truth.csv"))
//  val predictionFile1 = File("opt_north_east.csv")
//  val predictionFile2 = File("opt_north_west.csv")
//  val predictionFile3 = File("opt_south.csv")
//  val pred1 = scores.File2List(predictionFile1)
//  val pred2 = scores.File2List(predictionFile2)
//  val pred3 = scores.File2List(predictionFile3)
//  val predUnion = pred1 ++ pred2 ++ pred3
//  val pred = scores.removeDoubles(predUnion)
  val predictionFile = File("hmm_optim.csv")
  val pred = scores.File2List(predictionFile)

  def readShapefile(aFile: File, idAttribute: String) = {
    val store = new ShapefileDataStore(aFile.toJava.toURI.toURL)
    try {
      val reader = store.getFeatureReader
      var result = scala.collection.mutable.ArrayBuffer[(String, MultiLineString)]()
      while (reader.hasNext) {
        val feature = reader.next()
        val g = feature.getDefaultGeometry
        val geom = g match {
          case mls: MultiLineString => mls
          case _ => geomFactory.createMultiLineString(Array(g.asInstanceOf[LineString]))
        }
        val id = feature.getAttribute(idAttribute).toString.trim
        println(id)
        result.append((id, geom))
      }
      println(result.size)
      reader.close()
      Try{result.toMap}
    } finally store.dispose()
  }
  val geomFactory = new GeometryFactory
  println("read shp 1")
  val seq1 = readShapefile(file1, "ID").get
  println("read shp 2")
  val seq2 = readShapefile(file2, "ID").get
  println("matches")

  val tp = scores.removeDoubles(scores.truePositives(truth, pred))
  val fp = scores.removeDoubles(scores.falsePositives(truth, pred))
  val fn = scores.removeDoubles(scores.falseNegatives(truth, pred))

  val specs = "geom:LineString:srid=2154,ID1:String,ID2:String,type:String"
  try {
    val factory = new ShapefileDataStoreFactory
    val dataStore = factory.createDataStore(out.toJava.toURI.toURL)
    val featureTypeName = "Object"
    val featureType = DataUtilities.createType(featureTypeName, specs)
    dataStore.createSchema(featureType)
    val transaction = new DefaultTransaction("create")
    val typeName = dataStore.getTypeNames()(0)
    val featureSource = dataStore.getFeatureSource(typeName)
    val schema = featureSource.getSchema
    val featureStore = featureSource.asInstanceOf[SimpleFeatureStore]
    val collection = new ListFeatureCollection(featureType)
    featureStore.setTransaction(transaction)
    var i = 1
    def addFeature(list: List[(String,String)], matchType: String) = {
      list.foreach(v => {
        val id1 = v._1
        val id2 = v._2
        println("match btw " + id1 + " and " + id2)
        def getMiddleCoordinate(id: String, map: Map[String, MultiLineString]) = {
          val geom = map(id)
          new LengthIndexedLine(geom).extractPoint(geom.getLength/2)
        }
//        val g1 = seq1(id1)
//        val g2 = seq2(id2)
        val p1 = getMiddleCoordinate(id1, seq1)
        val p2 = getMiddleCoordinate(id2, seq2)
//        val geom = geomFactory.createLineString(Array(g1.getInteriorPoint.getCoordinate, g2.getInteriorPoint.getCoordinate))
        val geom = geomFactory.createLineString(Array(p1, p2))
        val values = Array[Object](geom, id1, id2, matchType)
        val simpleFeature = SimpleFeatureBuilder.build(schema, values, String.valueOf(i))
        collection.add(simpleFeature)
        i += 1
      })
    }
    addFeature(tp, "TP")
    addFeature(fp, "FP")
    addFeature(fn, "FN")
    try {
      featureStore.addFeatures(collection)
      transaction.commit()
    } catch {
      case problem: Exception =>
        problem.printStackTrace()
        transaction.rollback()
    } finally {
      transaction.close()
    }
  } catch {
    case e: Exception => e.printStackTrace()
  }

  //  seqMatches.foreach(m=>{
//    val geom = m._1
//    if (geom.getCoordinates.size != 2) println("Coordinates: " + geom.getCoordinates.size)
//    val p1 = if (revertGeometries) m._1.getCoordinates().last else m._1.getCoordinates().head
//    val l1 = getMatches(p1, index1)
//    val p2 = if (revertGeometries) m._1.getCoordinates().head else m._1.getCoordinates().last
//    val l2 = getMatches(p2, index2)
//    if (l1.size != 1) {
//      println(l1.size + " points in db1 for " + factory.createPoint(p1).toText)
//      val envelope = new Envelope(p1)
//      envelope.expandBy(10.0*tolerance)
//      println(factory.toGeometry(envelope))
//      val l = index1.query(envelope).toArray.map(_.asInstanceOf[(MultiLineString, Option[String], Option[String])])
//      println(l.size)
//      l1.foreach {
//        v=> println(v._3)
//      }
//    }
//    if (l2.size != 1) {
//      println(l2.size + " points in db2 for " + factory.createPoint(p2).toText)
//      val envelope = new Envelope(p2)
//      envelope.expandBy(tolerance * 10)
//      val l = index2.query(envelope).toArray.map(_.asInstanceOf[(MultiLineString, Option[String], Option[String])])
//      println(l.size)
//      l2.foreach {
//        v=> println(v._3)
//      }
//    }
//    if ((l1.size != 1) || (l2.size != 1)) {
//      val l1 = getMatches(p2, index1)
//      val l2 = getMatches(p1, index2)
//      if (l1.size != 1) {
//        println(l1.size + " points in db1 for " + factory.createPoint(p2).toText + " after invert")
//        l1.foreach {
//          v=> println(v._3)
//        }
//      }
//      if (l2.size != 1) {
//        println(l2.size + " points in db2 for " + factory.createPoint(p1).toText + " after invert")
//        l2.foreach {
//          v=> println(v._3)
//        }
//      }
//      if ((l1.size == 1) && (l2.size == 1)) {
//        println("invert worked")
//        val m1 = l1(0)
//        val m2 = l2(0)
//        //println("match " + m1._3 + " - " + m2._3)
//        writer.writeRow(List(m1._3.get, m2._3.get))
//        matchesCount = matchesCount + 1
//      } else {
//        println("FUCK")
//      }
//    } else {
//      if ((l1.size == 1) && (l2.size == 1)) {
//        val m1 = l1(0)
//        val m2 = l2(0)
//        //println("match " + m1._3 + " - " + m2._3)
//        writer.writeRow(List(m1._3.get, m2._3.get))
//        matchesCount = matchesCount + 1
//      } else {
//        println("FUCK IT")
//      }
//    }
//  })
//  writer.close
//  println(matchesCount + " matches")
}
