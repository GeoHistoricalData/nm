package fr.ign.nm

import better.files.File

object matchertest extends App {
  val workDirectory = File("../HMMSpatialNetworkMatcher/manual_matching")
//  val fileNetwork1 = "snapshot_1784.0_1791.0_edges.shp"
//  val fileNetwork2 = "snapshot_1825.0_1836.0_edges.shp"
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
//  val suffixes = List("_south","_north_east","_north_west")
  val suffixes = List("")
  val matches = scores.removeDoubles(suffixes.flatMap{suffix=>
    val fileNetwork1 = s"snapshot_1784.0_1791.0_edges$suffix.shp"
    val fileNetwork2 = s"snapshot_1825.0_1836.0_edges$suffix.shp"
    Hmmmatcher(workDirectory.toJava, fileNetwork1, fileNetwork2, idAttribute1, idAttribute2,
      //    lamdaAngular=27.945112126847874, lamdaFrechet=25.689741368712117, seed=1037889639764256252L, networkProjection=0.8269186748711304, parallelProcess=0.3497246088226589, pathMinLength=6.66491617095389, selectionThreshold=17.76237575023819)
//      lamdaFrechet = 30.5051686849836,
//      lamdaAngular = 12.6992766955633,
//      selectionThreshold = 13.5743349050397,
//      pathMinLength = 6.93678043301983,
      lamdaFrechet = 50.0,
      lamdaAngular = 23.6,//23.6133734307662,
      selectionThreshold = 14.0,//13.4505119461974,
      pathMinLength = 7.0,//7.42175693888132,
      optimizationPostStratregy = 1.0,
      networkProjection = 1.0,
      parallelProcess = 1.0,
      seed=seed)._1
    })
//  println(matches)
  val elementFile = File("truth.csv")
  val elem = scores.File2List(elementFile)
  val results = scores.compute(elem)(matches)
  println("precision = " + results._1)
  println("recall    = " + results._2)
  println("f1score    = " + results._3)
  scores.writeList(matches, File("hmm_optim.csv"))
//  println(scores.fscore(elem)(matches))
  //whole file: 0.9398613272958822
  //0.9318343532090507
  //doublehmm with previous params
//  precision = 0.9460005775339301
//  recall    = 0.9290981281905842
//  f1score    = 0.9374731721276292
//online params networkProjection = 0
//  precision = 0.951143125587222
//  recall    = 0.8613159387407827
//  f1score    = 0.904003571960113
  //networkProjection = 1
//  precision = 0.9440319817247287
//  recall    = 0.9376063528077141
//  f1score    = 0.9408081957882755
  //best calibration for optim with double hmm
//  precision = 0.94376248929489
//  recall    = 0.9376063528077141
//  f1score    = 0.9406743491250532
  // with optim
//  precision = 0.9357362391729533
//  recall    = 0.9498014747589336
//  f1score    = 0.9427163969035891

  //angular = 25
//  precision = 0.9343941931881631
//  recall    = 0.9492342597844583
//  f1score    = 0.941755768148565
  // angular = 23.6
//  precision = 0.9366591928251121
//  recall    = 0.94781622234827
//  f1score    = 0.9422046800112772
  //angular = 20
//  precision = 0.9337062937062937
//  recall    = 0.9466817923993194
//  f1score    = 0.9401492747500353
  //angular=23
}
