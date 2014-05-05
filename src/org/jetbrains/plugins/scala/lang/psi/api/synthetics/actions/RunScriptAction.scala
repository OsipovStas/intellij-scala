package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import javax.swing.Icon
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.compiler.PluginConnector
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.ScamFileType

/**
 * @author stasstels
 * @since  4/20/14.
 */
class RunScriptAction extends AnAction with TopComponentAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    runCompiler(e.getProject)
  }

  def runCompiler(p: Project) {
    Option(FileEditorManager.getInstance(p).getSelectedTextEditor).foreach {
      case editor =>
        PsiDocumentManager.getInstance(p).getPsiFile(editor.getDocument) match {
          case file: ScalaFile if file.getFileType.equals(ScamFileType) =>
            new PluginConnector(file).plug()
          case _ =>
        }

    }
  }

  override def actionIcon: Icon = AllIcons.Nodes.PluginRestart


  override def bundleKey: String = "annotations.plug.button"
}