package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import javax.swing.Icon
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.ui.{ScamScriptDescriptor, ScamScriptsWizard}
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.compiler.PluginConnector
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.SyntheticsProjectComponent

/**
 * @author stasstels
 * @since  5/9/14.
 */
class ScamScriptsWizardAction extends AnAction with TopComponentAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    ApplicationManager.getApplication.invokeLater {
      new Runnable {
        override def run(): Unit = {
          val project = e.getProject
          val component = SyntheticsProjectComponent.getInstance(project)
          val wizard = new ScamScriptsWizard(project)
          if (wizard.showAndGet()) {
            wizard.enabledScripts.foreach {
              case ScamScriptDescriptor(script, _)  =>
                SyntheticUtil.getScamPsiFile(script, project).foreach {
                  case psi: ScalaFile => new PluginConnector(psi).load()
                  case _ =>
                }
            }
            wizard.disabledScripts.foreach {
              case desc =>
                component.unplug(SyntheticUtil.scamName(desc.script))
            }

          }
        }
      }
    }
  }

  override def actionIcon: Icon = AllIcons.Nodes.DataSchema

  override def bundleKey: String = "synthetics.wizard.button"
}
