name := "sbt-js-test"

version := "0.8"

organization := "com.untyped"

scalaVersion := "2.10.4"

resolvers ++= Seq("snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "https://oss.sonatype.org/content/repositories/releases"
                )

seq(jsSettings : _*)

(sourceDirectories in (Compile, JsKeys.js)) <<= 
  (sourceDirectory in Compile) { main =>
    Seq(
      main / "js",
      main / "coffee"
    )
  }

(resourceManaged in (Compile, JsKeys.js)) <<= (resourceManaged in Compile)(_ / "js")

(compile in Compile) <<= compile in Compile dependsOn (JsKeys.js in Compile)

JsKeys.filenameSuffix in Compile <<= version ("-"+_+"-min")

JsKeys.sourceMaps in Compile := true

JsKeys.coffeeVersion in Compile := CoffeeVersion.Coffee180
