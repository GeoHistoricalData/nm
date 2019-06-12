package fr.ign.nm

import java.util.Calendar

import better.files.File
import scpsolver.lpsolver.{LPSOLVESolver, SolverFactory}

/**
  * Created by julien on 29/05/17.
  */
object opttest extends App {

//  val workDirectory = File("../HMMSpatialNetworkMatcher/manual_matching")
  val workDirectory = File("../HMMSpatialNetworkMatcher/DEP29")
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
  //  val id1 = "ID"
  //  val id2 = "ID"
  //  val a = 20.0
  //  val alpha = a
  //  val beta = 1.4
  //  val gamma = a
  //  val k = 6.0
  val id1 = "edge_id"
  val id2 = "id"
  val a = 500.0
  val alpha = a
  val beta = 3
  val gamma = a
  val k = 6.0
  //val selection = 1.0
  println(System.getProperty("java.library.path"))
  println(System.getProperty("java.class.path"))
  println(classOf[LPSOLVESolver].getClassLoader)
  import java.util.ServiceLoader
  import scpsolver.lpsolver.LinearProgramSolver
  println(ServiceLoader.load(classOf[LinearProgramSolver],classOf[LinearProgramSolver].getClassLoader))
  Thread.currentThread.setContextClassLoader(classOf[LPSOLVESolver].getClassLoader)
  val solver = SolverFactory.newDefault()
  println(solver)
  println(Calendar.getInstance.getTime + " Start")
  val start = System.currentTimeMillis
//  suffixes.foreach {
//    suffix =>
//      val fileNetwork1 = s"snapshot_1784.0_1791.0_edges$suffix.shp"
//      val fileNetwork2 = s"snapshot_1825.0_1836.0_edges$suffix.shp"
//      val out = s"_opt$suffix.csv"
////      val file1 = workDirectory / fileNetwork1
////      val file2 = workDirectory / fileNetwork2
//      opt(workDirectory.toJava, fileNetwork1, fileNetwork2, name1, name2, id1, id2, a, alpha, beta, gamma, k, File(out))
//  }
  val fileNetwork1 = s"cassini_4.shp"
  val fileNetwork2 = s"etat_major_4.shp"
  val out = s"cassini_em_4_opt.csv"
  opt(workDirectory.toJava, fileNetwork1, fileNetwork2, name1, name2, id1, id2, a, alpha, beta, gamma, k, File(out))

  val end = System.currentTimeMillis
  println(Calendar.getInstance.getTime + " End")
  println(s"Time elapsed: ${end - start}ms")
//  val matches = opt.buldMatches(file1, file2, name1, name2, id1, id2, a, alpha, beta, gamma, k)
//  val writer = CSVWriter.open(out)
//  matches.foreach {
//    m =>
//      println(m._1._3 + " => " + m._2._3)
//      writer.writeRow(List(m._1._3, m._2._3))
//  }
//  writer.close
}
