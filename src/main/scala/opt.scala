import better.files.File
import com.vividsolutions.jts.geom.{GeometryFactory, MultiLineString}
import org.geotools.data.shapefile.ShapefileDataStore
import scpsolver.constraints.{LinearBiggerThanEqualsConstraint, LinearSmallerThanEqualsConstraint}
import scpsolver.lpsolver.SolverFactory
import scpsolver.problems.LinearProgram

import scala.util.Try

/**
  * Created by julien on 24/05/17.
  */
object opt {
  type Feature = (MultiLineString, Option[String], String)
  def factory = new GeometryFactory
  def buldMatches(db1File: File, db2File: File, db1Name: Option[String], db2Name: Option[String], db1ID: String, db2ID: String,
                  a: Double, alpha: Double, beta: Double, gamma: Double, k: Double) = {
    def subModel(s1: Array[Feature], s2: Array[Feature],
                 similarity: Array[Double], delta: Seq[Double], length1: Array[Double], length2: Array[Double]) = {
      val lp = new LinearProgram(similarity)
      lp.setMinProblem(false)
      for (i <- (0 until lp.getIsboolean().length)) lp.getIsboolean()(i) = true
      // for all i: (sum of zij for all j <= 1)
      for (i <- (0 until s1.size)) {
        val a = Array.fill[Double](i * s2.size)(0.0)
        val b = Array.fill[Double](s2.size)(1.0)
        val c = Array.fill[Double]((s1.size - i - 1) * s2.size)(0.0)
        val weights = a ++ b ++ c
        lp.addConstraint(new LinearSmallerThanEqualsConstraint(weights, 1.0, "c1_" + i))
      }
      // for all j: (sum of zij for all i + delta_j >= 1)
      for (j <- (0 until s2.size)) {
        val weights = (0 until s1.size * s2.size).toArray.map { i => if (((i - j) % s2.size) == 0) 1.0 else 0.0 }
        lp.addConstraint(new LinearBiggerThanEqualsConstraint(weights, (1.0 - delta(j)), "c2_" + j))
      }
      // for all j: (sum of zij * li for all i <= kj*beta)
      for (j <- (0 until s2.size)) {
        val weights = (0 until s1.size * s2.size).toArray.map { i => if (((i - j) % s2.size) == 0) length1(i / s2.size) else 0.0 }
        lp.addConstraint(new LinearSmallerThanEqualsConstraint(weights, beta * length2(j), "c3_" + j))
      }
      val solver = SolverFactory.newDefault()
      val solution = solver.solve(lp)
      System.out.println("solution " + solution.length)
      solution.zipWithIndex.filter(_._1 == 1.0).map { case (v: Double, i: Int) => (s1(i / s2.size), s2(i % s2.size)) }
    }

    def binarySlackArray(p: Int, q: Int, similarity: Array[Double]) = {
      for {
        i <- (0 until p)
        j <- (0 until q)
        distance = similarity(i + j * p)
        delta = if (distance > gamma) 0.0 else 1.0
      } yield delta
    }.toArray

    def lengthArray(s: Array[Feature]) = {
      for (f <- s) yield f._1.getLength
    }.toArray

    def similarityMatrix(s1: Array[Feature], s2: Array[Feature]) = {
      for (i <- (0 until s1.size); j <- (0 until s2.size)) yield similarity(s1(i), s2(j))
    }.toArray

    def similarity(f1: Feature, f2: Feature) = {
      val d = directedHausdorf(f1._1, f2._1)
      if (d > a) {
        0.0
      } else {
        if (!f1._2.isDefined || !f2._2.isDefined) {
          a - d
        } else {
          val nd = nameDissimilarity(f1._2.get, f2._2.get)
          if (nd < 0) a - d
          else a - (d + nd) / 2.0;
        }
      }
    }

    def directedHausdorf(l1: MultiLineString, l2: MultiLineString) = {
      l1.getCoordinates.map(c => l2.distance(factory.createPoint(c))).max
    }

    def nameDissimilarity(n1: String, n2: String) = {
      val minLength = Math.min(n1.length, n2.length)
      val lengthDifference = Math.abs(n1.length - n2.length)
      val d = hammingDistance(n1.substring(0, minLength), n2.substring(0, minLength)) + lengthDifference
      2.0 * d / (n1.length + n2.length()) * alpha
    }

    def hammingDistance(n1: String, n2: String) = n1.zip(n2).count(c => c._1.equals(c._2))

    def readShapefile(aFile: File, nameAttribute: Option[String], idAttribute: String) = {
      val store = new ShapefileDataStore(aFile.toJava.toURI.toURL)
      try {
        val reader = store.getFeatureReader
        try {
//          val result = while (reader.hasNext) {
//            val feature = reader.next
//            println("feature")
//            val geom = feature.getDefaultGeometry.asInstanceOf[MultiLineString]
//            val name = feature.getAttribute(nameAttribute).toString
//            val id = feature.getAttribute(idAttribute).toString
//            (geom, name, id)
//          }
          Try {
            val featureReader = Iterator.continually(reader.next).takeWhile(_ => reader.hasNext)
            val result = featureReader.map { feature =>
              println("feature")
              val geom = feature.getDefaultGeometry.asInstanceOf[MultiLineString]
              val name = if (nameAttribute.isDefined) Some(feature.getAttribute(nameAttribute.get).toString) else None
              val id = feature.getAttribute(idAttribute).toString
              (geom, name, id)
            }.toArray
            result
          }
        } finally reader.close
      } finally store.dispose
    }

    println("read shp 1")
    val seq1 = readShapefile(db1File, db1Name, db1ID).get
    println("read shp 2")
    val seq2 = readShapefile(db2File, db2Name, db2ID).get
    println("compute sim 1")
    // compute similarity matrix
    val similarityMatrix1 = similarityMatrix(seq1, seq2)
    println("compute sim 2")
    val similarityMatrix2 = similarityMatrix(seq2, seq1)
    val lengthArray1 = lengthArray(seq1)
    val lengthArray2 = lengthArray(seq2)
    println("compute bin slack arrays")
    val delta1 = binarySlackArray(seq1.size, seq2.size, similarityMatrix2)
    val delta2 = binarySlackArray(seq2.size, seq1.size, similarityMatrix1)
    println("compute submodel 1")
    val subModel1Links = subModel(seq1, seq2, similarityMatrix1, delta2, lengthArray1, lengthArray2)
    println("compute submodel 2")
    val subModel2Links = subModel(seq2, seq1, similarityMatrix2, delta1, lengthArray2, lengthArray1)
    println("compute model")
    subModel1Links ++ subModel2Links
  }
}
