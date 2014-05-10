package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.actions


import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import javax.swing.Icon
import com.intellij.icons.AllIcons
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.{SyntheticsProjectComponent, ScamFileType}

/**
 * @author stasstels
 * @since  4/20/14.
 */
class StopScriptsAction extends AnAction with TopComponentAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    doUnplug(e.getProject)
  }

  private def doUnplug(p: Project) {
    Option(FileEditorManager.getInstance(p).getSelectedTextEditor).foreach {
      case editor =>
        PsiDocumentManager.getInstance(p).getPsiFile(editor.getDocument) match {
          case file: ScalaFile if file.getFileType.equals(ScamFileType) =>
            SyntheticsProjectComponent.getInstance(p).unplug(SyntheticUtil.scamName(file))
          case _ =>
        }

    }
  }


  override def actionIcon: Icon = AllIcons.Nodes.PluginUpdate


  override def bundleKey: String = "synthetics.unplug.button"
}
