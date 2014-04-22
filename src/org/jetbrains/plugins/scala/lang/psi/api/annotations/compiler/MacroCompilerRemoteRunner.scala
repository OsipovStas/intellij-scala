package org.jetbrains.plugins.scala
package lang.psi.api.annotations.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.jps.incremental.scala.remote.RemoteResourceOwner
import java.io.File
import com.intellij.openapi.util.io.FileUtil
import java.net._
import org.jetbrains.plugins.scala.compiler.ScalaApplicationSettings
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.util.{Base64Converter, PathUtil}
import org.jetbrains.jps.incremental.scala.data.SbtData
import org.jetbrains.plugin.scala.compiler.IncrementalType
import org.jetbrains.plugins.scala
import org.jetbrains.jps.incremental.scala.Client
import com.intellij.openapi.module.ModuleManager

/**
 * @author stasstels
 * @since  4/21/14.
 */
class MacroCompilerRemoteRunner(project: Project) extends RemoteResourceOwner {

  implicit def file2path(file: File) = FileUtil.toCanonicalPath(file.getAbsolutePath)

  implicit def option2string(opt: Option[String]) = opt getOrElse ""

  implicit def files2paths(files: Iterable[File]) = files.map(file2path).mkString("\n")

  implicit def array2string(arr: Array[String]) = arr mkString "\n"

  protected val address = InetAddress.getByName(null)

  protected val port =
    try
      Integer parseInt settings.COMPILE_SERVER_PORT
    catch {
      case e: NumberFormatException =>
        throw new IllegalArgumentException("Bad port: " + ScalaApplicationSettings.getInstance().COMPILE_SERVER_PORT, e)
    }

  val projectClassPath = CompilerParameters.getClasspath(project)

  val libRoot: File = new File(PathUtil.getJarPathForClass(getClass)).getParentFile()

  val canonicalLib = PathUtil.getCanonicalPath(libRoot.getPath)

  val sbtData = SbtData.from(
    new URLClassLoader(Array(new URL("jar:file:" + (if (canonicalLib startsWith "/") "" else "/") + canonicalLib + "/jps/sbt-interface.jar!/")), getClass.getClassLoader),
    new File(libRoot, "jps"),
    new File(System.getProperty("user.home"), ".idea-build"),
    System.getProperty("java.class.version")
  ) match {
    case Left(msg) => throw new IllegalArgumentException(msg)
    case Right(data) => data
  }

  private val scalaParameters = Array.empty[String]

  private val javaParameters = Array.empty[String]

  private val compilerJar = new File(canonicalLib, "scala-compiler.jar")

  private val libraryJar = new File(canonicalLib, "scala-library.jar")

  private val reflectJar = new File(canonicalLib, "scala-reflect.jar")

  private val runnersJar = new File(canonicalLib, "scala-plugin-runners.jar")

  private val compilerJars = Seq(libraryJar, compilerJar) :+ reflectJar

  private val compilerSettingsJar = new File(canonicalLib, "compiler-settings.jar")


  def compile(src: File, out: File, client: Client) {


    val cp = projectClassPath ++ Seq(reflectJar, compilerJar, libraryJar, runnersJar, compilerSettingsJar, out)

    val arguments = Seq[String](
      sbtData.interfaceJar,
      sbtData.sourceJar,
      sbtData.interfacesHome,
      sbtData.javaClassVersion,
      compilerJars,
      findJdk,
      src,
      cp,
      out,
      scalaParameters,
      javaParameters,
      settings.COMPILE_ORDER.toString,
      "", //cache file
      "",
      "",
      IncrementalType.IDEA.name(),
      src.getParentFile,
      out,
      Array.empty[String]
    )

    try
      send(serverAlias, arguments map (s => Base64Converter.encode(s getBytes "UTF-8")), client)
    catch {
      case e: ConnectException =>
        val message = "Cannot connect to compile server at %s:%s".format(address.toString, port)
        client.error(message)
      case e: UnknownHostException =>
        val message = "Unknown IP address of compile server host: " + address.toString
        client.error(message)
    }


  }


  private def settings = ScalaApplicationSettings.getInstance()

  private def configurationError(message: String) = throw new IllegalArgumentException(message)

  def findJdk = scala.compiler.findJdkByName(settings.COMPILE_SERVER_SDK) match {
    case Right(jdk) => jdk.executable
    case Left(msg) =>
      configurationError(s"Cannot find jdk ${settings.COMPILE_SERVER_SDK} for compile server, underlying message: $msg")
  }

}

object CompilerParameters {

  def getClasspath(p: Project) = ModuleManager.getInstance(p).getModules.toSeq.flatMap {
    case module => OrderEnumerator.orderEntries(module).compileOnly().getClassesRoots.toSeq map {
      case f => new File(f.getCanonicalPath stripSuffix "!" stripSuffix "!/")
    }
  }

}