package fr.ign.nm

import java.io.File
import java.util.Random

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.{CompositeEmissionProbabilityStrategy, ITransitionProbabilityStrategy, PathBuilder, PostProcessStrategy}
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.ep.FrechetEmissionProbability
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.pathbuilder.StrokePathBuilder
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.postProcessStrategy.{OptimizationPostStratregy, ReverseHMMPostStrategy}
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.{HMMMatchingLauncher, ParametersSet}
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.tp.AngularTransitionProbability
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.io.HMMExporter

import scala.collection.JavaConverters._

object Hmmmatcher {
  def run(fileNetwork1: File, fileNetwork2: File, idAttribute1:String, idAttribute2:String,
          lamdaFrechet: Double, lamdaAngular: Double, selectionThreshold:Double,
          pathMinLength:Int, networkProjection: Boolean, parallelProcess: Boolean,
          optimizationPostStratregy: Boolean, seed: Long):(List[(String, String)], Long) = {
    val time_start = System.nanoTime
    val epStrategy: CompositeEmissionProbabilityStrategy = new CompositeEmissionProbabilityStrategy
    epStrategy.add(new FrechetEmissionProbability(lamdaFrechet), 1d)
    val tpStrategy: ITransitionProbabilityStrategy = new AngularTransitionProbability(lamdaAngular)
    val pathBuilder: PathBuilder = new StrokePathBuilder
    val postProcressStrategy: PostProcessStrategy = if (optimizationPostStratregy) new OptimizationPostStratregy else new ReverseHMMPostStrategy()
    ParametersSet.get.SELECTION_THRESHOLD = selectionThreshold
    ParametersSet.get.NETWORK_PROJECTION = networkProjection
    ParametersSet.get.PATH_MIN_LENGTH = pathMinLength
    def generator = new Random(seed)
    val matchingLauncher: HMMMatchingLauncher = new HMMMatchingLauncher(fileNetwork1.toURI.toString, fileNetwork2.toURI.toString, epStrategy, tpStrategy, pathBuilder, postProcressStrategy, parallelProcess, generator)
    matchingLauncher.lauchMatchingProcess()
    val exporter = new HMMExporter
    import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader
    val inRef = ShapefileReader.read(fileNetwork1.getAbsolutePath)
    val inComp = ShapefileReader.read(fileNetwork2.getAbsolutePath)
    val r = exporter.exportWithID(matchingLauncher.getMatching, inRef, inComp, idAttribute1, idAttribute2)
    val list = r.asScala.map(l=>(l.get(0),l.get(1))).toList
    val time_end = System.nanoTime
    (list, time_end - time_start)
  }

  def runFromDouble(fileNetwork1: File, fileNetwork2: File, idAttribute1:String, idAttribute2:String,
                    lamdaFrechet: Double, lamdaAngular: Double, selectionThreshold:Double,
                    pathMinLength:Double, networkProjection: Double, parallelProcess: Double,
                    optimizationPostStratregy: Double, seed: Long): (List[(String, String)], Long) = {
    run(fileNetwork1, fileNetwork2, idAttribute1, idAttribute2, lamdaFrechet, lamdaAngular, selectionThreshold, pathMinLength.toInt, networkProjection > 0.5, parallelProcess > 0.5, optimizationPostStratregy > 0.5, seed)
  }
  def runTask(directory: File, fileNetwork1: String, fileNetwork2: String, idAttribute1:String, idAttribute2:String,
              lamdaFrechet: Double, lamdaAngular: Double, selectionThreshold:Double,
              pathMinLength:Double, networkProjection: Double, parallelProcess: Double,
              optimizationPostStratregy: Double, seed: Long): (List[(String, String)], Long) = {
    runFromDouble(new File(directory, fileNetwork1), new File(directory, fileNetwork2), idAttribute1, idAttribute2, lamdaFrechet, lamdaAngular, selectionThreshold, pathMinLength, networkProjection, parallelProcess, optimizationPostStratregy, seed)
  }
  def apply(directory: File, fileNetwork1: String, fileNetwork2: String, idAttribute1:String, idAttribute2:String,
            lamdaFrechet: Double, lamdaAngular: Double, selectionThreshold:Double,
            pathMinLength:Double, networkProjection: Double, parallelProcess: Double,
            optimizationPostStratregy: Double, seed: Long): (List[(String, String)], Long) = {
    runTask(directory, fileNetwork1, fileNetwork2, idAttribute1, idAttribute2, lamdaFrechet, lamdaAngular, selectionThreshold, pathMinLength, networkProjection, parallelProcess, optimizationPostStratregy, seed)
  }
}