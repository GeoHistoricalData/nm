organization := "fr.ign"

name := "nm"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

val monocleVersion = "1.4.0"

val geotoolsVersion = "19.1"

//val jtsVersion = "1.14.0"

//val breezeVersion = "0.13.1"

val scpVersion = "20180615"

//val morphogenesisVersion = "1.0-SNAPSHOT"

//crossSbtVersions := Vector("0.13.16", "1.0.2")

resolvers ++= Seq(
  "osgeo" at "http://download.osgeo.org/webdav/geotools/",
  "geosolutions" at "http://maven.geo-solutions.it/",
  "geotoolkit" at "http://maven.geotoolkit.org/",
  "ign-releases" at "https://forge-cogit.ign.fr/nexus/content/repositories/releases/"
)
resolvers += Resolver.typesafeRepo("releases")
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq (
  "org.geotools" % "gt-referencing" % geotoolsVersion,
  "org.geotools" % "gt-shapefile" % geotoolsVersion,
  "org.geotools" % "gt-epsg-wkt" % geotoolsVersion,
  "org.geotools" % "gt-cql" % geotoolsVersion,
  "org.geotools" % "gt-geotiff" % geotoolsVersion,
  "org.geotools" % "gt-image" % geotoolsVersion,
  "org.geotools" % "gt-coverage" % geotoolsVersion,
//  "com.vividsolutions" % "jts" % jtsVersion,
  "com.github.pathikrit" %% "better-files" % "2.17.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "org.scpsolver" % "scpsolver" % scpVersion,
  "org.scpsolver" % "lpsolvesolverpack" % scpVersion,
  //"fr.ign.cogit" % "morphogenesis" % morphogenesisVersion
  "fr.ign.cogit" % "HMMSpatialNetworkMatcher" % "0.0.1-SNAPSHOT"
    excludeAll(
    ExclusionRule(organization = "org.neo4j"),
    ExclusionRule(organization = "postgresql"),
    ExclusionRule(organization = "net.java.dev.jna"),
    ExclusionRule(organization = "org.postgresql"),
    ExclusionRule(organization = "fr.ign.cogit", name = "geoxygene-ontology"),
    ExclusionRule(organization = "fr.ign.cogit", name = "geoxygene-sig3d")
  )
)

//updateOptions := updateOptions.value.withLatestSnapshots(false)

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

enablePlugins(SbtOsgi)

osgiSettings

OsgiKeys.exportPackage := Seq("fr.ign.nm.*")

OsgiKeys.importPackage := Seq("*;resolution:=optional")

OsgiKeys.privatePackage := Seq("!scala.*,!java.*,*")

//OsgiKeys.embeddedJars := (Keys.externalDependencyClasspath in Compile).value map (_.data) filter (x=>(x.name.startsWith("gt-")))
//||(x.name.startsWith("lpsolvesolverpack"))

OsgiKeys.requireCapability := ""

OsgiKeys.additionalHeaders := Map("Bundle-NativeCode" -> "lib/liblpsolve55j_x64.so ; osname = Linux ; processor = x86-64")
