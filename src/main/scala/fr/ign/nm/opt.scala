package fr.ign.nm

import better.files._
import com.github.tototoshi.csv.CSVWriter
import org.geotools.data.shapefile.ShapefileDataStore
import org.locationtech.jts.geom.{GeometryFactory, MultiLineString}
import scpsolver.constraints.{LinearBiggerThanEqualsConstraint, LinearSmallerThanEqualsConstraint}
import scpsolver.lpsolver.SolverFactory
import scpsolver.problems.LinearProgram

import scala.util.Try

/**
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
      for (i <- lp.getIsboolean.indices) lp.getIsboolean()(i) = true
      // for all i: (sum of zij for all j <= 1)
      for (i <- s1.indices) {
        val a = Array.fill[Double](i * s2.length)(0.0)
        val b = Array.fill[Double](s2.length)(1.0)
        val c = Array.fill[Double]((s1.length - i - 1) * s2.length)(0.0)
        val weights = a ++ b ++ c
        lp.addConstraint(new LinearSmallerThanEqualsConstraint(weights, 1.0, "c1_" + i))
      }
      // for all j: (sum of zij for all i + delta_j >= 1)
      for (j <- s2.indices) {
        val weights = (0 until s1.length * s2.length).toArray.map { i => if (((i - j) % s2.length) == 0) 1.0 else 0.0 }
        lp.addConstraint(new LinearBiggerThanEqualsConstraint(weights, 1.0 - delta(j), "c2_" + j))
      }
      // for all j: (sum of zij * li for all i <= kj*beta)
      for (j <- s2.indices) {
        val weights = (0 until s1.length * s2.length).toArray.map { i => if (((i - j) % s2.length) == 0) length1(i / s2.length) else 0.0 }
        lp.addConstraint(new LinearSmallerThanEqualsConstraint(weights, beta * length2(j), "c3_" + j))
      }
      val solver = SolverFactory.newDefault()
      val solution = solver.solve(lp)
      System.out.println("solution " + solution.length)
      solution.zipWithIndex.filter(_._1 == 1.0).map { case (v: Double, i: Int) => (s1(i / s2.length), s2(i % s2.length)) }
    }

    def binarySlackArray(p: Int, q: Int, similarity: Array[Double]) = {
      for {
        i <- 0 until p
        j <- 0 until q
        distance = similarity(i + j * p)
        delta = if (distance > gamma) 0.0 else 1.0
      } yield delta
    }.toArray

    def lengthArray(s: Array[Feature]) = for (f <- s) yield f._1.getLength

    def similarityMatrix(s1: Array[Feature], s2: Array[Feature]) = {
      for (i <- s1.indices; j <- s2.indices) yield similarity(s1(i), s2(j))
    }.toArray

    def similarity(f1: Feature, f2: Feature) = {
      val d = directedHausdorf(f1._1, f2._1)
      if (d > a) {
        0.0
      } else {
        if (f1._2.isEmpty || f2._2.isEmpty) {
          a - d
        } else {
          val nd = nameDissimilarity(f1._2.get, f2._2.get)
          if (nd < 0) a - d
          else a - (d + nd) / 2.0
        }
      }
    }

    def directedHausdorf(l1: MultiLineString, l2: MultiLineString) = l1.getCoordinates.map(c => l2.distance(factory.createPoint(c))).max

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
          Try {
            val featureReader = Iterator.continually(reader.next).takeWhile(_ => reader.hasNext)
            val result = featureReader.map { feature =>
              val geom = feature.getDefaultGeometry.asInstanceOf[MultiLineString]
              val name = if (nameAttribute.isDefined) Some(feature.getAttribute(nameAttribute.get).toString) else None
              val id = feature.getAttribute(idAttribute).toString
              (geom, name, id)
            }.toArray
            result
          }
        } finally reader.close()
      } finally store.dispose()
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
    val delta1 = binarySlackArray(seq1.length, seq2.length, similarityMatrix2)
    val delta2 = binarySlackArray(seq2.length, seq1.length, similarityMatrix1)
    println("compute submodel 1")
    val subModel1Links = subModel(seq1, seq2, similarityMatrix1, delta2, lengthArray1, lengthArray2)
    println("compute submodel 2")
    val subModel2Links = subModel(seq2, seq1, similarityMatrix2, delta1, lengthArray2, lengthArray1)
    println("compute model")
    subModel1Links ++ subModel2Links.map{case (la, lb)=>(lb, la)}
  }
  def apply(directory: java.io.File, db1File: String, db2File: String, db1Name: String, db2Name: String, db1ID: String, db2ID: String,
            a: Double, alpha: Double, beta: Double, gamma: Double, k: Double):List[(String, String)] = {
    opt.buldMatches(new java.io.File(directory, db1File).toScala, new java.io.File(directory, db2File).toScala, Option(db1Name), Option(db2Name), db1ID, db2ID, a, alpha, beta, gamma, k).map { m => (m._1._3, m._2._3) }.toList
  }

  def apply(directory: java.io.File, db1File: String, db2File: String, db1Name: String, db2Name: String, db1ID: String, db2ID: String,
            a: Double, alpha: Double, beta: Double, gamma: Double, k: Double, out: File) = {
    val matches = opt.buldMatches(new java.io.File(directory, db1File).toScala, new java.io.File(directory, db2File).toScala, Option(db1Name), Option(db2Name), db1ID, db2ID, a, alpha, beta, gamma, k).map { m => (m._1._3, m._2._3) }.toList
    val writer = CSVWriter.open(out.toJava)
    matches.foreach { m => writer.writeRow(List(m._1, m._2)) }
    writer.close
  }
}
