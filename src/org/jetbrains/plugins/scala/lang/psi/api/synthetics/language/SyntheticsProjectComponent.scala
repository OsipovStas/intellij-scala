package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.language

import com.intellij.openapi.project.Project
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.{FileEditorManager, FileEditorManagerEvent, FileEditorManagerListener}
import com.intellij.openapi.vfs.VirtualFile
import java.awt.FlowLayout
import javax.swing.JPanel
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import scala.collection.mutable
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.actions.{ScamScriptsWizardAction, StopScriptsAction, RunScriptAction}

/**
 * @author stasstels
 * @since  4/28/14.
 */
class SyntheticsProjectComponent(project: Project) extends ProjectComponent {

  override def disposeComponent(): Unit = {}

  override def initComponent(): Unit = {}

  override def projectClosed(): Unit = {}

  override def projectOpened(): Unit = {
    project.getMessageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, SyntheticsEditorListener)
  }

  override def getComponentName: String = "Synthetic definitions"


  private object SyntheticsEditorListener extends FileEditorManagerListener {

    override def selectionChanged(event: FileEditorManagerEvent): Unit = {
    }

    override def fileClosed(source: FileEditorManager, file: VirtualFile): Unit = {
      source.getAllEditors(file).foreach {
        case e =>
          SyntheticsProjectComponent.getAndRemovePanel(file).foreach {
            case p => source.removeTopComponent(e, p)
          }
      }
    }

    override def fileOpened(source: FileEditorManager, file: VirtualFile): Unit = {
      if (file.getFileType == ScamFileType) {
        initTopComponents(file)
      }
    }

    def initTopComponents(file: VirtualFile) {
      if (project.isDisposed) return

      val manager = FileEditorManager.getInstance(project)
      manager.getAllEditors(file).foreach {
        case e =>
          val panel = new JPanel(new FlowLayout(FlowLayout.LEFT))
          buttons.foreach(_.init(panel))
          manager.addTopComponent(e, panel)
      }

    }

    def buttons: Seq[TopComponentAction] = Seq(new ScamScriptsWizardAction, new StopScriptsAction, new RunScriptAction)

  }

}

object SyntheticsProjectComponent {

  private val file2panel = mutable.WeakHashMap[VirtualFile, JPanel]()

  def getAndRemovePanel(file: VirtualFile) = file2panel.remove(file)


}
