package org.jetbrains.plugins.scala
package lang.psi.api.annotations.action

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import org.jetbrains.plugins.scala.worksheet.actions.TopComponentAction
import javax.swing.Icon
import com.intellij.icons.AllIcons
import org.jetbrains.plugins.scala.lang.psi.api.annotations.base.SyntheticAnnotations

/**
 * @author stasstels
 * @since  4/20/14.
 */
class UnplugAnnotationAction extends AnAction with TopComponentAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    SyntheticAnnotations.unplug()
  }


  override def actionIcon: Icon = AllIcons.Nodes.PluginUpdate


  override def bundleKey: String = "annotations.unplug.button"
}
