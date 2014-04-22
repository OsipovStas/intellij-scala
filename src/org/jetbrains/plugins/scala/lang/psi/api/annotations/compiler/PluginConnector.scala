package org.jetbrains.plugins.scala
package lang.psi.api.annotations.compiler

import com.intellij.compiler.progress.CompilerTask
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.jps.incremental.scala.Client
import java.io.File
import com.intellij.openapi.progress.ProgressManager
import org.jetbrains.jps.incremental.messages.BuildMessage.Kind
import org.jetbrains.jps.incremental.messages.BuildMessage
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.compiler.CompilerMessageImpl
import java.net.{URLClassLoader, URL}
import scala.util.Try
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.MacroAnnotation
import org.jetbrains.plugins.scala.compiler.CompileServerLauncher
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.plugins.scala.lang.psi.api.annotations.compiler.MacroCompiler.MacroCompilerTask
import com.intellij.openapi.application.ApplicationManager

/**
 * @author stasstels
 * @since  4/21/14.
 */
class PluginConnector(template: ScalaFile) {

  val task = new CompilerTask(template.getProject, "Load Macro Annotation", false, false, false, false)

  val client = new TaskClientAdapter(task) {
    override def compilationEnd(): Unit = if (!hasErrors) load()
  }

  def plug() = {
    CompileServerLauncher.instance.tryToStart(template.getProject)
    val tempFile = FileUtil.createTempFile("macro", null, true)
    FileUtil.writeToFile(tempFile, template.getText)
    MacroCompiler.runCompileTask(MacroCompilerTask(task, client, tempFile))
  }


  def load() = {
    ApplicationManager.getApplication.invokeLater {
      new Runnable {
        override def run(): Unit = {
          template.typeDefinitions.map(_.getQualifiedName).filter(_ != null).foreach {
            case name =>
              val ma = MacroAnnotationLoadFactory.newInstance(name, CompilerParameters.getClasspath(template.getProject) :+ MacroCompiler.out)
              println(s"Loaded $ma")
          }
          println("loaded")
        }
      }
    }
  }
}

abstract class TaskClientAdapter(task: CompilerTask) extends Client {

  var hasErrors = false

  override def isCanceled: Boolean = false

  override def deleted(module: File): Unit = {}

  override def processed(source: File): Unit = {}

  override def generated(source: File, module: File, name: String): Unit = {}

  override def debug(text: String): Unit = {}

  override def progress(text: String, done: Option[Float]): Unit = {
    val taskIndicator = ProgressManager.getInstance().getProgressIndicator

    if (taskIndicator != null) {
      taskIndicator setText text
      done map (d => taskIndicator.setFraction(d.toDouble))
    }
  }

  override def trace(exception: Throwable): Unit = {
    throw new RuntimeException(exception)
  }

  override def message(kind: Kind, text: String, source: Option[File], line: Option[Long], column: Option[Long]): Unit = {

    val category = kind match {
      case BuildMessage.Kind.INFO => CompilerMessageCategory.INFORMATION
      case BuildMessage.Kind.ERROR =>
        hasErrors = true
        CompilerMessageCategory.ERROR
      case BuildMessage.Kind.PROGRESS => CompilerMessageCategory.STATISTICS
      case BuildMessage.Kind.WARNING => CompilerMessageCategory.WARNING
    }

    task.addMessage(new CompilerMessageImpl(task.getProject, category, text))
  }
}


class MacroAnnotationLoader(urls: Seq[File], parent: ClassLoader) extends URLClassLoader(urls.map(_.toURI.toURL).toArray, parent) {
  override def loadClass(name: String): Class[_] = {
    Try(super.loadClass(name)).getOrElse {
      findClass(name)
    }
  }
}


object MacroAnnotationLoadFactory {

  def newInstance(className: String, classpath: Seq[File]): Option[MacroAnnotation] = {
    val ru = reflect.runtime.universe
    val rm = ru.runtimeMirror(new MacroAnnotationLoader(classpath, MacroAnnotation.getClass.getClassLoader))
    Try(rm.staticClass(className)).flatMap {
      case clazz => Try(clazz.toType.declaration(ru.nme.CONSTRUCTOR).asMethod).flatMap {
        case constructor =>
          Try(rm.reflectClass(clazz).reflectConstructor(constructor)() match {
            case ma: MacroAnnotation => ma
          })
      }
    }.toOption
  }

}
