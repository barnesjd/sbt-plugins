package com.untyped.sbtjs

import com.google.javascript.jscomp.{
  SourceFile => ClosureSource,
  Compiler => ClosureCompiler,
  CompilerOptions => ClosureOptions,
  SourceMap
}
import sbt._
import scala.collection.JavaConversions._
import java.lang.StringBuilder 

trait Source extends com.untyped.sbtgraph.Source {

  type S = com.untyped.sbtjs.Source
  type G = com.untyped.sbtjs.Graph

  def compile: Seq[File] = {
    val desJs = this.des.headOption getOrElse (throw new Exception("Could not determine destination filename for " + src))
    val desMap = this.des.lastOption

    graph.log.info("Compiling %s source %s".format(graph.pluginName, desJs))

    val compiler = new ClosureCompiler

    ClosureCompiler.setLoggingLevel(graph.closureLogLevel)

    val myExterns = graph.closureExterns(this)
    val mySources = graph.closureSources(this)

    graph.log.debug("  externs:")
    myExterns.foreach(x => graph.log.debug("    " + x))

    graph.log.debug("  sources:")
    mySources.foreach(x => graph.log.debug("    " + x))

    val options = if(graph.sourceMaps) {
      val name = desMap.map(_.getCanonicalPath) getOrElse (throw new Exception("Could not determine destination source map filename for " + src))
      val opts = graph.closureOptions.clone.asInstanceOf[ClosureOptions]
      graph.log.info("Generating map at %s".format(name))
      opts.setSourceMapOutputPath(name)
      opts.setSourceMapDetailLevel(SourceMap.DetailLevel.ALL)
      opts.setSourceMapFormat(SourceMap.Format.V3)
      opts
    }
    else graph.closureOptions

    val result =
      compiler.compile(
        myExterns,
        mySources,
        options)

    val errors = result.errors.toList
    val warnings = result.warnings.toList

    if(!errors.isEmpty) {
      graph.log.error(errors.length + " errors compiling " + src + ":")
      errors.foreach(err => graph.log.error(err.toString))

      Seq.empty
    } else {
      if(!warnings.isEmpty) {
        graph.log.warn(warnings.length + " warnings compiling " + src + ":")
        warnings.foreach(err => graph.log.warn(err.toString))
      }

      IO.createDirectory(new File(desJs.getParent))
      IO.write(desJs, compiler.toSource)

      if(graph.sourceMaps) {
        val sb = new StringBuilder
        result.sourceMap.appendTo(sb, "yo")
        desMap.map(f => IO.write(f, sb.toString))
      }

      Seq(desJs) ++ desMap
    }
  }

  // Helpers ------------------------------------

  /** Closure externs for this file (not its parents). */
  def closureExterns: List[ClosureSource] = Nil

  /** Closure sources for this file (not its parents). */
  def closureSources: List[ClosureSource]


}
