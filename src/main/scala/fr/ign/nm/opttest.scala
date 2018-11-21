package fr.ign.nm

import better.files.File
import com.github.tototoshi.csv.CSVWriter
import scpsolver.lpsolver.{LPSOLVESolver, SolverFactory}

/**
  * Created by julien on 29/05/17.
  */
object opttest extends App {

  val workDirectory = File("manual_matching")
//  val fileNetwork1 = "snapshot_1784.0_1791.0_edges.shp"
//  val fileNetwork2 = "snapshot_1825.0_1836.0_edges.shp"
  val suffixes = List("_south","_north_east","_north_west")
//  val file1 = File("/home/julien/data/networkmatching/matching/snapshot_1784.0_1791.0_edges_north.shp")
//  val file2 = File("/home/julien/data/networkmatching/matching/snapshot_1825.0_1836.0_edges_north.shp")
  /*
  val file1 = File("/home/julien/devel/nm/src/main/resources/jac.shp")
  val file2 = File("/home/julien/devel/nm/src/main/resources/ver.shp")
  val name1 = "NOM_ENTIER"
  val name2 = "NOM_ENTIER"
  val id1 = "gid"
  val id2 = "gid"
  */
  val name1 = null
  val name2 = null
  val id1 = "ID"
  val id2 = "ID"
  val a = 20.0
  val alpha = a
  val beta = 1.4
  val gamma = a
  val k = 6.0
  //val selection = 1.0
  println(System.getProperty("java.library.path"))
  println(System.getProperty("java.class.path"))
  println(classOf[LPSOLVESolver].getClassLoader)
  import java.util.ServiceLoader
  import scpsolver.lpsolver.{LinearProgramSolver}
  println(ServiceLoader.load(classOf[LinearProgramSolver],classOf[LinearProgramSolver].getClassLoader))
  Thread.currentThread.setContextClassLoader(classOf[LPSOLVESolver].getClassLoader)
  val solver = SolverFactory.newDefault()
  println(solver)
  suffixes.foreach {
    suffix =>
      val fileNetwork1 = s"snapshot_1784.0_1791.0_edges$suffix.shp"
      val fileNetwork2 = s"snapshot_1825.0_1836.0_edges$suffix.shp"
      val out = s"_opt$suffix.csv"
//      val file1 = workDirectory / fileNetwork1
//      val file2 = workDirectory / fileNetwork2
      opt(workDirectory.toJava, fileNetwork1, fileNetwork2, name1, name2, id1, id2, a, alpha, beta, gamma, k, File(out))
  }
//  val matches = opt.buldMatches(file1, file2, name1, name2, id1, id2, a, alpha, beta, gamma, k)
//  val writer = CSVWriter.open(out)
//  matches.foreach {
//    m =>
//      println(m._1._3 + " => " + m._2._3)
//      writer.writeRow(List(m._1._3, m._2._3))
//  }
//  writer.close
}
