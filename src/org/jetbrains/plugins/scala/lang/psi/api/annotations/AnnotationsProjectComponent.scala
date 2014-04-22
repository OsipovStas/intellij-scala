package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.{FileEditorManagerEvent, FileEditorManager, FileEditorManagerListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JPanel
import java.awt.FlowLayout
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import scala.collection.mutable
import org.jetbrains.plugins.scala.lang.psi.api.annotations.action.{UnplugAnnotationAction, PlugAnnotationAction}

/**
 * @author stasstels
 * @since  4/14/14.
 */
class AnnotationsProjectComponent(project: Project) extends ProjectComponent {

  override def disposeComponent(): Unit = {}

  override def initComponent(): Unit = {}

  override def projectClosed(): Unit = {}

  override def projectOpened(): Unit = {
    project.getMessageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, AnnotationsEditorListener)
  }

  override def getComponentName: String = "Macro Annotations"


  private object AnnotationsEditorListener extends FileEditorManagerListener {

    override def selectionChanged(event: FileEditorManagerEvent): Unit = {
    }

    override def fileClosed(source: FileEditorManager, file: VirtualFile): Unit = {
      source.getAllEditors(file).foreach {
        case e =>
          AnnotationsProjectComponent.getAndRemovePanel(file).foreach {
            case p => source.removeTopComponent(e, p)
          }
      }
    }

    override def fileOpened(source: FileEditorManager, file: VirtualFile): Unit = {
      if (ScalaFileType.MACRO_ANNOTATION_FILE_EXTENSION == file.getExtension) {
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

    def buttons: Seq[TopComponentAction] = Seq(new PlugAnnotationAction, new UnplugAnnotationAction)

  }

}

object AnnotationsProjectComponent {

  private val file2panel = mutable.WeakHashMap[VirtualFile, JPanel]()

  def getAndRemovePanel(file: VirtualFile) = file2panel.remove(file)


}
