package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.language

import com.intellij.openapi.project.Project
import com.intellij.openapi.components._
import com.intellij.openapi.fileEditor.{FileEditorManager, FileEditorManagerEvent, FileEditorManagerListener}
import com.intellij.openapi.vfs.VirtualFile
import java.awt.FlowLayout
import javax.swing.JPanel
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import scala.collection.mutable
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.actions.{ScamScriptsWizardAction, StopScriptsAction, RunScriptAction}
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import com.intellij.psi.PsiManager
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.compiler.PluginConnector
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.SyntheticsProjectComponent.State
import com.intellij.openapi.components
import scala.beans.BeanProperty
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.base.BeanScript
import org.jetbrains.plugins.scala.dsl.base.ScamScript
import com.intellij.openapi.util.text
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.application.ApplicationManager

/**
 * @author stasstels
 * @since  4/28/14.
 */


@components.State(
  name = "PlugedScamScripts",
  storages = Array(
    new Storage(file = StoragePathMacros.PROJECT_FILE),
    new Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/scam.xml", scheme = StorageScheme.DIRECTORY_BASED)
  )
)
class SyntheticsProjectComponent(project: Project) extends ProjectComponent with PersistentStateComponent[SyntheticsProjectComponent.State] {

  private var state = new State()

  private val baseScripts = Seq(BeanScript)

  private var scamScripts: Seq[ScamScript] = baseScripts

  def register(script: ScamScript): Unit = {
    val scriptName = script.getClass.getCanonicalName
    val scripts = scamScripts.filterNot {
      case s =>
        text.StringUtil.equals(s.getClass.getCanonicalName, scriptName)
    }
    scamScripts = script +: scripts
  }

  def scripts: Seq[ScamScript] = scamScripts

  def unplug(name: String) = {
    scamScripts = scamScripts.filterNot(s => StringUtil.equals(s.getClass.getCanonicalName, name))
  }

  def unplug() = {
    scamScripts = baseScripts
  }

  def isPluged(file: VirtualFile) = {
    scamScripts.exists {
      case script => StringUtil.equals(script.getClass.getCanonicalName, SyntheticUtil.scamName(file))
    }
  }


  override def getState: State = {
    import scala.collection.JavaConversions._
    new State(scamScripts.map(_.getClass.getCanonicalName))
  }

  override def loadState(state: State): Unit = {
    this.state = state
  }

  override def disposeComponent(): Unit = {
  }

  override def initComponent(): Unit = {}

  override def projectClosed(): Unit = {

  }


  override def projectOpened(): Unit = {
    project.getMessageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, SyntheticsEditorListener)
    import scala.collection.JavaConversions._
    ApplicationManager.getApplication.invokeLater {
      new Runnable {
        override def run(): Unit = loadState(state.scripts.toSet)
      }
    }
  }

  private def loadState(oldState: Set[String]) = {
    val psiManager = PsiManager.getInstance(project)
    SyntheticUtil.findScamScripts(project).filter {
      case script => oldState.contains(SyntheticUtil.scamName(script))
    } flatMap {
      case script => Option(psiManager.findFile(script))
    } foreach {
      case psi: ScalaFile => new PluginConnector(psi).load()
      case _ =>
    }
  }


  override def getComponentName: String = "SyntheticProjectComponent"


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

  class State {

    def this(scripts: java.util.List[String]) = {
      this()
      this.scripts = scripts
    }

    @BeanProperty
    var scripts: java.util.List[String] = new java.util.ArrayList[String]()
  }

  def getAndRemovePanel(file: VirtualFile) = file2panel.remove(file)

  def getInstance(p: Project) = p.getComponent(classOf[SyntheticsProjectComponent])

}
