package fr.ign.nm

import better.files.File
import scpsolver.lpsolver.{LPSOLVESolver, LinearProgramSolver, SolverFactory}

object matchertest extends App {
  val workDirectory = File("../HMMSpatialNetworkMatcher/manual_matching")
  val fileNetwork1 = "snapshot_1784.0_1791.0_edges.shp"
  val fileNetwork2 = "snapshot_1825.0_1836.0_edges.shp"
  val idAttribute1 = "ID"
  val idAttribute2 = "ID"
//  val lamdaFrechet = 1.0
//  val lamdaAngular = 1.0
//  val selectionThreshold = 10.0
//  val pathMinLength = 1.0
//  val networkProjection = 0.0
//  val parallelProcess = 1.0

//  println(System.getProperty("java.library.path"))
//  Thread.currentThread.setContextClassLoader(classOf[LPSOLVESolver].getClassLoader)
//  val solver = SolverFactory.newDefault()
//  println(solver)
//
//  import scpsolver.lpsolver.LinearProgramSolver
//  import java.util.ServiceLoader
//
//  val loader = ServiceLoader.load(classOf[LinearProgramSolver])
//  println(loader)
  val seed = 42L
//  val matches = Hmmmatcher(workDirectory.toJava, fileNetwork1, fileNetwork2, idAttribute1, idAttribute2,
//    lamdaFrechet, lamdaAngular, selectionThreshold, pathMinLength, networkProjection, parallelProcess, seed)
  val matches = Hmmmatcher(workDirectory.toJava, fileNetwork1, fileNetwork2, idAttribute1, idAttribute2,
//    lamdaAngular=27.945112126847874, lamdaFrechet=25.689741368712117, seed=1037889639764256252L, networkProjection=0.8269186748711304, parallelProcess=0.3497246088226589, pathMinLength=6.66491617095389, selectionThreshold=17.76237575023819)
  lamdaAngular=27.945112126847874, lamdaFrechet=25.689741368712117, seed=1037889639764256252L, networkProjection=0.8269186748711304, parallelProcess=0.3497246088226589, pathMinLength=6.66491617095389, selectionThreshold=17.76237575023819)
  println(matches)
//  val elementFile = File("manuel.csv")
  val elementFile = File("truth.csv")
  val elem = scores.File2List(elementFile)
//  println(scores.fscore(elem.map { case (a,b) => (b,a) })(matches))
  println(scores.fscore(elem)(matches))
  //val m = hmmmatching.impl.HMMMatchingLauncher.runStringFromDouble(fileNetwork1,fileNetwork2,idAttribute1,idAttribute2,selection,stroke_length, resampling, lpsolving)
  //println(m)
}
