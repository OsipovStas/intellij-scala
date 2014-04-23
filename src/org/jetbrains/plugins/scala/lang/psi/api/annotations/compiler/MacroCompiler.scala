package org.jetbrains.plugins.scala
package lang.psi.api.annotations.compiler

import com.intellij.openapi.util.io.FileUtil
import com.intellij.compiler.progress.CompilerTask
import org.jetbrains.jps.incremental.scala.Client
import java.io.File

/**
 * @author stasstels
 * @since  4/21/14.
 */
object MacroCompiler {

  val out = FileUtil.createTempDirectory("macros", null, true)


  def runCompileTask(task: MacroCompilerTask) {
    val runner = new MacroCompilerRemoteRunner(task.project)
    task.start {
      new Runnable {
        override def run(): Unit = {
          runner.compile(task.code, out, task.client)
        }
      }
    }

  }

  case class MacroCompilerTask(task: CompilerTask, client: Client, code: File) {

    val project = task.getProject

    def start(r: Runnable) = task.start(r, new Runnable {
      override def run(): Unit = {}
    })
  }


}
