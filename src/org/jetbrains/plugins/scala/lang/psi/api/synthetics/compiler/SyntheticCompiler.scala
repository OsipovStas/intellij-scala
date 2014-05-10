package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.compiler

import com.intellij.compiler.progress.CompilerTask
import org.jetbrains.jps.incremental.scala.Client
import java.io.File
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.DSL

/**
 * @author stasstels
 * @since  4/21/14.
 */
object SyntheticCompiler {

  def out = DSL.scamScriptDir


  def runCompileTask(task: SyntheticCompilerTask) {
    val runner = new SyntheticCompilerRemoteRunner(task.project)
    task.start {
      new Runnable {
        override def run(): Unit = {
          runner.compile(task.code, out, task.client)
        }
      }
    }

  }

  case class SyntheticCompilerTask(task: CompilerTask, client: Client, code: File) {

    val project = task.getProject

    def start(r: Runnable) = task.start(r, new Runnable {
      override def run(): Unit = {}
    })
  }


}
