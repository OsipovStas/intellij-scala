package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import javax.swing.Icon
import com.intellij.icons.AllIcons

/**
 * @author stasstels
 * @since  4/20/14.
 */
class StopScriptsAction extends AnAction with TopComponentAction {

  override def actionPerformed(e: AnActionEvent): Unit = {}


  override def actionIcon: Icon = AllIcons.Nodes.PluginUpdate


  override def bundleKey: String = "annotations.unplug.button"
}
