package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.OrderEnumerator
import java.io.File
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.ScamFileType
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.DSL
import org.apache.commons.io.FilenameUtils

object CompilerParameters {

  def getClasspath(p: Project) = ModuleManager.getInstance(p).getModules.toSeq.flatMap {
    case module => OrderEnumerator.orderEntries(module).compileOnly().getClassesRoots.toSeq map {
      case f => new File(f.getCanonicalPath stripSuffix "!" stripSuffix "!/")
    }
  }


  def getScamName(file: ScalaFile): Option[String] = file match {
    case script if script.getFileType.equals(ScamFileType) => Some(FilenameUtils.removeExtension(script.getName) + DSL.SCAM_MARKER)
    case _ => None
  }

  def getScamText(template: ScalaFile): String = {
    if (template.getFileType.equals(ScamFileType)) {
      val script = template.getText
      val className = getScamName(template).getOrElse(DSL.SCAM_MARKER)
      s"""
         |import org.jetbrains.plugins.scala.dsl.types.Context
         |import org.jetbrains.plugins.scala.dsl.base.ScamScript
         |
         |class $className extends ScamScript {
         |  override def run()(implicit ctx: Context): Unit = {
         |    $script
         |  }
         |}
       """.stripMargin
    } else ""
  }

}